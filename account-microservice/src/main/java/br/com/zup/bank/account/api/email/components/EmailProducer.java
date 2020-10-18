
package br.com.zup.bank.account.api.email.components;

import br.com.zup.bank.account.api.email.configs.SenderConfig;
import br.com.zup.bank.account.api.email.models.EmailInfo;
import java.util.UUID;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EmailProducer {
    @Value("${email.pending.topic}")
    private String emailPendingTopic;
    
    public void sendEmailToProcess(EmailInfo emailInfo){
        String message = serialize(emailInfo);
        KafkaTemplate<String, String> template = SenderConfig.getInstance().getKafkaTemplate();
        final String messageKey = UUID.randomUUID().toString();
        template.send(emailPendingTopic, messageKey, message);
    }
    
    private String serialize(EmailInfo emailInfo){
        JSONObject json = new JSONObject();
        json.put("destinatary", emailInfo.getDestinatary());
        json.put("subject", emailInfo.getSubject());
        json.put("message", emailInfo.getMessage());
        
        return json.toString();
    }
}
