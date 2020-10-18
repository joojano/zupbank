package br.com.zup.bank.account.api.email.components;

import br.com.zup.bank.account.api.email.configs.desserializers.DeserializerEmailInfo;
import br.com.zup.bank.account.api.email.models.EmailInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailSender {

    @Autowired
    private JavaMailSender emailSender;
    
    @KafkaListener(topics = "email_pending", groupId = "consumer_email_pending")
    public void consumeAndSend(ConsumerRecord consumerRecord) {
        String json = consumerRecord.value().toString();
        EmailInfo emailInfo = new DeserializerEmailInfo().deserialize(json);
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply.zupbank@gmail.com");
        message.setTo(emailInfo.getDestinatary());
        message.setSubject(emailInfo.getSubject());
        message.setText(emailInfo.getMessage());
        emailSender.send(message);
        log.info("[EMAIL] - Action: New account created. Destinatary: " + emailInfo.getDestinatary());
    }
}
