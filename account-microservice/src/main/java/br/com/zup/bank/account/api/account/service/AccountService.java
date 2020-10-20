package br.com.zup.bank.account.api.account.service;

import br.com.zup.bank.account.api.account.domain.AbstractAccountOperations;
import br.com.zup.bank.account.api.account.domain.models.Account;
import br.com.zup.bank.account.api.account.domain.models.FirstAccessModel;
import br.com.zup.bank.account.api.account.domain.models.FirstPasswordModel;
import br.com.zup.bank.account.api.account.domain.models.Token;
import br.com.zup.bank.account.api.account.domain.models.proposal.Proposal;
import br.com.zup.bank.account.api.account.domain.models.transfer.Transfer;
import br.com.zup.bank.account.api.account.repository.AccountRepository;
import br.com.zup.bank.account.api.account.repository.TransferRepository;
import br.com.zup.bank.account.api.kafka.models.EmailInfo;
import br.com.zup.bank.account.api.kafka.components.KafkaProducer;
import br.com.zup.bank.account.api.utils.Utils;
import br.com.zup.bank.api.proposal.domain.enums.StatusApprovalEnum;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class AccountService implements AbstractAccountOperations {

    @Value("${proposal.apiKey}")
    private String apiKey;

    //@Value("${proposal.refreshKey}")
    //private String refreshKey;
    @Value("${proposal.getUrl}")
    private String url;

    @Value("${proposal.bankNumber}")
    private String bankNumber;

    @Value("${token.minutes.duration}")
    private int duration;

    private final long ONE_MINUTE_IN_MILLIS = 60000;

    @Autowired
    private JavaMailSender emailSender;

    private final RestTemplate restTemplate;

    private final KafkaProducer kafkaProducer;

    public AccountService(RestTemplateBuilder restTemplateBuilder, KafkaProducer emailProducer) {
        this.restTemplate = restTemplateBuilder.build();
        this.kafkaProducer = emailProducer;
    }

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransferRepository transferRepository;

    private long calculateTokenInvalidAfter(int duration) {
        return new Date(Calendar.getInstance().getTimeInMillis() + (duration * ONE_MINUTE_IN_MILLIS)).getTime();
    }

    private boolean checkPassword(String password) {
        return Utils.isRegexMatch("(^\\d{8}$)", password);
    }

    @Override
    public ResponseEntity createFirstPassword(FirstPasswordModel firstPasswordModel) {
        Optional<Account> accountReturn = accountRepository.findAccountByToken(firstPasswordModel.getToken());
        if (!accountReturn.isPresent()) return Utils.returnNotFoundMessage();
        if (accountReturn.get().getToken().getInvalidAfter() < Calendar.getInstance().getTimeInMillis()) return Utils.returnBadRequestMessage("Token expired.");
        if (accountReturn.get().getToken().isTokenUsed()) return Utils.returnBadRequestMessage("Token already used.");
        if (!checkPassword(firstPasswordModel.getPassword())) return Utils.returnBadRequestMessage("Password must contain 8 digits");
        Token token = Token.builder()
                .minutesTokenDuration(accountReturn.get().getToken().getMinutesTokenDuration())
                .createdAt(accountReturn.get().getToken().getCreatedAt())
                .invalidAfter(accountReturn.get().getToken().getInvalidAfter())
                .isTokenUsed(true)
                .token(accountReturn.get().getToken().getToken())
                .build();
        Account account = Account.builder()
                .id(accountReturn.get().getId())
                .agencyNumber(accountReturn.get().getAgencyNumber())
                .accountNumber(accountReturn.get().getAccountNumber())
                .bankNumber(accountReturn.get().getBankNumber())
                .password(DigestUtils.sha1Hex(firstPasswordModel.getPassword()))
                .token(token)
                .balance(accountReturn.get().getBalance())
                .proposalId(accountReturn.get().getProposalId())
                .build();
        accountRepository.save(account);
        Proposal proposal = getProposalEntityById(account.getProposalId());
        sendUpdatedPassword(proposal);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity createNewAccount(String proposalId) {
        Optional<Account> accountReturn = accountRepository.findByProposalId(proposalId);

        if (accountReturn.isPresent()) {
            return Utils.returnConflict("The provided proposal ID already has been converted into account");
        }

        Proposal proposalReturn = getProposalEntityById(proposalId);
        if (Objects.isNull(proposalReturn)) {
            return Utils.returnNotFoundMessage();
        }

        if (proposalReturn.getSteps().getIsAcceptedByBank() != StatusApprovalEnum.APPROVED
                || proposalReturn.getSteps().getIsAcceptedByCustomer() != StatusApprovalEnum.APPROVED) {
            return Utils.returnUnprocessableEntity("The proposal doesn't have the appropriated approvals to be converted into account");
        }

        Account account = Account.builder()
                .proposalId(proposalId)
                .agencyNumber(generateDigits(4))
                .accountNumber(generateDigits(8))
                .bankNumber(bankNumber)
                .balance(BigDecimal.ZERO)
                .build();

        accountRepository.insert(account);

        sendCreatedAccountEmail(account, proposalReturn.getCustomer().getEmail());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity firstAccess(FirstAccessModel firstAccessModel) {
        Proposal proposal = getProposalEntityByEmailAndCpf(firstAccessModel.getEmail(), firstAccessModel.getCpf());
        if (Objects.isNull(proposal)) {
            return Utils.returnNotFoundMessage();
        }
        Optional<Account> accountResponse = accountRepository.findByProposalId(proposal.getId());
        if (!accountResponse.isPresent()) {
            return Utils.returnBadRequestMessage("The proposal is not converted into account yet.");
        }
        Token token = Token.builder()
                .createdAt(new Date().getTime())
                .invalidAfter(calculateTokenInvalidAfter(duration))
                .isTokenUsed(false)
                .minutesTokenDuration(duration)
                .token(generateDigits(6))
                .build();
        Account account = Account.builder()
                .id(accountResponse.get().getId())
                .agencyNumber(accountResponse.get().getAgencyNumber())
                .accountNumber(accountResponse.get().getAccountNumber())
                .bankNumber(accountResponse.get().getBankNumber())
                .token(token)
                .balance(accountResponse.get().getBalance())
                .proposalId(accountResponse.get().getProposalId()).build();
        accountRepository.save(account);

        sendCreatedTokenEmail(token, proposal.getCustomer().getEmail());

        return ResponseEntity.ok().build();
    }

    private Proposal getProposalEntityByEmailAndCpf(String email, String cpf) {
        String urlGetProposal = url.concat("?email=").concat(email).concat("&cpf=").concat(cpf);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        HttpEntity request = new HttpEntity(headers);
        ResponseEntity<Proposal> response = this.restTemplate.exchange(urlGetProposal, HttpMethod.GET, request, Proposal.class, 1);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }
        return null;
    }

    private Proposal getProposalEntityById(String proposalId) {
        String urlGetProposal = url.concat("?id=").concat(proposalId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        HttpEntity request = new HttpEntity(headers);
        ResponseEntity<Proposal> response = this.restTemplate.exchange(urlGetProposal, HttpMethod.GET, request, Proposal.class, 1);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }
        return null;
    }

    public void processTransfer(Transfer transfer) {
        if (transferRepository.findSameTransferByBank(transfer.getBankOrigin(), transfer.getUniqueCodeOrigin()).isPresent()) {
            log.info("[TRANSFER][ALREADY_EXISTS]: Transferência com código "
                    + transfer.getUniqueCodeOrigin() + " do banco "
                    + transfer.getBankOrigin() + " já existente no banco de dados. Ignorando transferência.");
            return;
        }
        Optional<Account> accountResponse = accountRepository.findAccountByAgencyAndNumber(transfer.getAgencyDestiny(), transfer.getAccountDestiny());
        if (!accountResponse.isPresent()) {
            log.info("[TRANSFER][NOT_FOUND] Transferência com código " + transfer.getUniqueCodeOrigin() + " do banco "
                    + transfer.getBankOrigin() + " solicita transferência a agência/conta não existentes."
                    + " Dados: Conta: " + transfer.getAccountDestiny()
                    + " Agência: " + transfer.getAgencyDestiny());
            return;
        }
        Account account = accountResponse.get();
        BigDecimal oldBalance = account.getBalance();
        BigDecimal newBalance = account.getBalance().add(transfer.getAmmount());
        account.setBalance(newBalance);

        transferRepository.insert(transfer);
        accountRepository.save(account);
        log.info("[TRANSFER][SUCCESS] Transferência com código " + transfer.getUniqueCodeOrigin() + " do banco "
                + transfer.getBankOrigin() + " foi efetuada com sucesso."
                + "\nDados: Conta: " + transfer.getAccountDestiny()
                + "\nAgência: " + transfer.getAgencyDestiny()
                + "\nBalanço anterior: " + oldBalance.toPlainString()
                + "\nBalanço atualizado: " + newBalance.toPlainString());
    }

    @Override
    public ResponseEntity receiveTransfers(ArrayList<Transfer> transferInfo) {
        for (Transfer transfer : transferInfo) {
            kafkaProducer.sendTransferToProcess(transfer);
        }
        return ResponseEntity.ok().build();
    }

    private void sendCreatedAccountEmail(Account account, String emailCustomer) {
        String message = "A sua nova conta da Zupbank foi aprovada e criada! Segue os dados de sua nova conta: \n"
                + "Código do banco: " + account.getBankNumber() + "\n"
                + "Agência: " + account.getAgencyNumber() + "\n"
                + "Número da conta: " + account.getAccountNumber();
        EmailInfo emailInfo = EmailInfo.builder()
                .destinatary(emailCustomer)
                .subject("Sua nova conta da Zupbank foi criada")
                .message(message).build();

        kafkaProducer.sendEmailToProcess(emailInfo);
    }

    private String generateDigits(int len) {
        SecureRandom sr = new SecureRandom();
        String result = (sr.nextInt(9) + 1) + "";
        for (int i = 0; i < len - 2; i++) {
            result += sr.nextInt(10);
        }
        result += (sr.nextInt(9) + 1);
        return result;
    }

    private void sendCreatedTokenEmail(Token token, String email) {
        String message = "Foi solicitado um token para realizar o primeiro acesso a sua conta. "
                + "\nUse o seguinte token para criar a primeira senha de acesso a sua conta: " + token.getToken();
        EmailInfo emailInfo = EmailInfo.builder()
                .destinatary(email)
                .subject("Seu token para primeiro acesso a sua conta")
                .message(message).build();
        
        kafkaProducer.sendEmailToProcess(emailInfo);
    }

    private void sendUpdatedPassword(Proposal proposal) {
        String message = "A senha da sua conta foi modificada. Caso não foi você, entre em contato com o nosso suporte ao cliente.";
        EmailInfo emailInfo = EmailInfo.builder()
                .destinatary(proposal.getCustomer().getEmail())
                .subject("Sua senha foi modificada")
                .message(message).build();
        
        kafkaProducer.sendEmailToProcess(emailInfo);
    }

}
