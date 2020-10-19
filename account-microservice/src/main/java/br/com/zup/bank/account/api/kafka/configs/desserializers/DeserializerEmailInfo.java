
package br.com.zup.bank.account.api.kafka.configs.desserializers;

import br.com.zup.bank.account.api.kafka.models.EmailInfo;
import org.json.JSONObject;

public class DeserializerEmailInfo {
    public EmailInfo deserialize(String json){
        return deserializerEmailInfo(new JSONObject(json));
    }

    private EmailInfo deserializerEmailInfo(JSONObject jsonObject) {
        return EmailInfo.builder()
                .destinatary(jsonObject.optString("destinatary"))
                .subject(jsonObject.optString("subject"))
                .message(jsonObject.optString("message"))
                .build();
    }
}
