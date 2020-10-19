
package br.com.zup.bank.account.api.kafka.components;

import br.com.zup.bank.account.api.account.domain.models.transfer.Transfer;
import br.com.zup.bank.account.api.kafka.configs.SenderConfig;
import br.com.zup.bank.account.api.kafka.models.EmailInfo;
import java.math.RoundingMode;
import java.util.UUID;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {
    @Value("${email.pending.topic}")
    private String emailPendingTopic;
    
    @Value("${transfer.pending.topic}")
    private String transferPendingTopic;
    
    public void sendEmailToProcess(EmailInfo emailInfo){
        String message = serializeEmail(emailInfo);
        KafkaTemplate<String, String> template = SenderConfig.getInstance().getKafkaTemplate();
        final String messageKey = UUID.randomUUID().toString();
        template.send(emailPendingTopic, messageKey, message);
    }
    
    public void sendTransferToProcess(Transfer transfer){
        String message = serializeTransfer(transfer);
        KafkaTemplate<String, String> template = SenderConfig.getInstance().getKafkaTemplate();
        final String messageKey = UUID.randomUUID().toString();
        template.send(transferPendingTopic, messageKey, message);
    }
    
    private String serializeEmail(EmailInfo emailInfo){
        JSONObject json = new JSONObject();
        json.put("destinatary", emailInfo.getDestinatary());
        json.put("subject", emailInfo.getSubject());
        json.put("message", emailInfo.getMessage());
        
        return json.toString();
    }
    
    private String serializeTransfer(Transfer transfer){
        JSONObject json = new JSONObject();
        json.put("ammount", transfer.getAmmount().setScale(2, RoundingMode.HALF_UP));
        json.put("realizationDate", transfer.getRealizationDate().getTime());
        json.put("idDocumentOrigin", transfer.getIdDocumentOrigin());
        json.put("bankOrigin", transfer.getBankOrigin());
        json.put("accountOrigin", transfer.getAccountOrigin());
        json.put("agencyOrigin", transfer.getAgencyOrigin());
        json.put("uniqueCodeOrigin", transfer.getUniqueCodeOrigin());
        json.put("accountDestiny", transfer.getAccountDestiny());
        json.put("agencyDestiny", transfer.getAgencyDestiny());
        return json.toString();
    }
}
