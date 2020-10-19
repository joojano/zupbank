
package br.com.zup.bank.account.api.account.service;

import br.com.zup.bank.account.api.account.domain.AbstractAccountOperations;
import br.com.zup.bank.account.api.account.domain.models.Account;
import br.com.zup.bank.account.api.account.domain.models.transfer.GetTransferModel;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public ResponseEntity createNewAccount(String proposalId) {
        Optional<Account> accountReturn = accountRepository.findByProposalId(proposalId);
        
        if (accountReturn.isPresent()) return Utils.returnConflict("The provided proposal ID already has been converted into account");
        
        Proposal proposalReturn = getProposalEntity(proposalId);
        if (Objects.isNull(proposalReturn)) return Utils.returnNotFoundMessage();
        
        if (proposalReturn.getSteps().getIsAcceptedByBank() != StatusApprovalEnum.APPROVED 
                || proposalReturn.getSteps().getIsAcceptedByCustomer() != StatusApprovalEnum.APPROVED) return Utils.returnUnprocessableEntity("The proposal doesn't have the appropriated approvals to be converted into account");
        
        Account account = Account.builder()
                .proposalId(proposalId)
                .agencyNumber(generateDigits(4))
                .accountNumber(generateDigits(8))
                .bankNumber(bankNumber)
                .balance(BigDecimal.ZERO)
                .build();
        
        accountRepository.insert(account);
        
        sendEmail(account, proposalReturn.getCustomer().getEmail());
        return ResponseEntity.ok().build();
    }
    
    private Proposal getProposalEntity(String proposalId){
        String urlGetProposal = url.concat(proposalId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        HttpEntity request = new HttpEntity(headers);
        ResponseEntity<Proposal> response = this.restTemplate.exchange(urlGetProposal, HttpMethod.GET, request, Proposal.class, 1);
        
        if (response.getStatusCode() == HttpStatus.OK) return response.getBody();
        return null;
    }

    @Override
    public ResponseEntity getTransfer(GetTransferModel transferModel) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        //ArrayList<Transfer> transfers = new ArrayList<Transfer>(transferInfo);
        for (Transfer transfer : transferInfo) {
            kafkaProducer.sendTransferToProcess(transfer);
        }
        return ResponseEntity.ok().build();
    }
        
    private void sendEmail(Account account, String emailCustomer){
        String message = "A sua nova conta da Zupbank foi aprovada e criada! Segue os dados de sua nova conta: \n"
                + "Código do banco: " + account.getBankNumber() + "\n"
                        + "Agência: " + account.getAgencyNumber()+ "\n"
                                + "Número da conta: " + account.getAccountNumber();
        EmailInfo emailInfo = EmailInfo.builder()
                .destinatary(emailCustomer)
                .subject("Sua nova conta da Zupbank foi criada")
                .message(message).build();
        
        kafkaProducer.sendEmailToProcess(emailInfo);
    }
    
    private String generateDigits(int len){
        SecureRandom sr = new SecureRandom();
        String result = (sr.nextInt(9)+1) +"";
        for(int i=0; i<len-2; i++) result += sr.nextInt(10);
        result += (sr.nextInt(9)+1);
        return result;
    }

}
