
package br.com.zup.bank.account.api.account.service;

import br.com.zup.bank.account.api.account.domain.AbstractAccountOperations;
import br.com.zup.bank.account.api.account.domain.models.Account;
import br.com.zup.bank.account.api.account.domain.models.Proposal;
import br.com.zup.bank.account.api.account.repository.AccountRepository;
import br.com.zup.bank.account.api.email.models.EmailInfo;
import br.com.zup.bank.account.api.email.components.EmailProducer;
import br.com.zup.bank.account.api.utils.Utils;
import br.com.zup.bank.api.proposal.domain.enums.StatusApprovalEnum;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Optional;
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
    
    private final EmailProducer emailProducer;

    public AccountService(RestTemplateBuilder restTemplateBuilder, EmailProducer emailProducer) {
        this.restTemplate = restTemplateBuilder.build();
        this.emailProducer = emailProducer;
    }
    
    @Autowired
    private AccountRepository accountRepository;

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
        
    private void sendEmail(Account account, String emailCustomer){
        String message = "A sua nova conta da Zupbank foi aprovada e criada! Segue os dados de sua nova conta: \n"
                + "Código do banco: " + account.getBankNumber() + "\n"
                        + "Agência: " + account.getAgencyNumber()+ "\n"
                                + "Número da conta: " + account.getAccountNumber();
        EmailInfo emailInfo = EmailInfo.builder()
                .destinatary(emailCustomer)
                .subject("Sua nova conta da Zupbank foi criada")
                .message(message).build();
        
        emailProducer.sendEmailToProcess(emailInfo);
        
        /*SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply.zupbank@gmail.com");
        message.setTo(emailCustomer);
        message.setSubject("Sua nova conta da Zupbank foi criada!");
        message.setText("A sua nova conta da Zupbank foi aprovada e criada! Segue os dados de sua nova conta: \n"
                + "Código do banco: " + account.getBankNumber() + "\n"
                        + "Agência: " + account.getAgencyNumber()+ "\n"
                                + "Número da conta: " + account.getAccountNumber());
        emailSender.send(message);*/
    }
    
    private String generateDigits(int len){
        SecureRandom sr = new SecureRandom();
        String result = (sr.nextInt(9)+1) +"";
        for(int i=0; i<len-2; i++) result += sr.nextInt(10);
        result += (sr.nextInt(9)+1);
        return result;
    }

}
