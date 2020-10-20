package br.com.zup.bank.account.api.kafka.components;

import br.com.zup.bank.account.api.account.domain.models.transfer.Transfer;
import br.com.zup.bank.account.api.account.service.AccountService;
import br.com.zup.bank.account.api.kafka.configs.desserializers.DeserializerEmailInfo;
import br.com.zup.bank.account.api.kafka.configs.desserializers.DeserializerTransfer;
import br.com.zup.bank.account.api.kafka.models.EmailInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaConsumer {

    @Autowired
    private JavaMailSender emailSender;
    
    @Autowired
    private AccountService accountService;
    
    @KafkaListener(topics = "email_pending", groupId = "consumer_email_pending")
    public void consumeEmailAndSend(ConsumerRecord consumerRecord) {
        String json = consumerRecord.value().toString();
        EmailInfo emailInfo = new DeserializerEmailInfo().deserialize(json);
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply.zupbank@gmail.com");
        message.setTo(emailInfo.getDestinatary());
        message.setSubject(emailInfo.getSubject());
        message.setText(emailInfo.getMessage());
        emailSender.send(message);
        log.info("[EMAIL] New email with subject " + emailInfo.getSubject() + " sent to: " + emailInfo.getDestinatary());
    }
    
    @KafkaListener(topics = "transfer_pending", groupId = "consumer_transfer_pending")
    public void consumeTransferAndCommit(ConsumerRecord consumerRecord) {
        String json = consumerRecord.value().toString();
        Transfer transfer = new DeserializerTransfer().deserialize(json);
        accountService.processTransfer(transfer);
    }
}
