
package br.com.zup.bank.account.api.kafka.configs.desserializers;

import br.com.zup.bank.account.api.account.domain.models.transfer.Transfer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import org.json.JSONObject;

public class DeserializerTransfer {

    public Transfer deserialize(String json) {
        return deserializerTransferInfo(new JSONObject(json));
    }
    
    private Transfer deserializerTransferInfo(JSONObject jsonObject){
        return Transfer.builder()
                .ammount(jsonObject.optBigDecimal("ammount", BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP))
                .realizationDate(new Date(jsonObject.optLong("realizationDate")))
                .idDocumentOrigin(jsonObject.optString("idDocumentOrigin"))
                .bankOrigin(jsonObject.optString("bankOrigin"))
                .accountOrigin(jsonObject.optString("accountOrigin"))
                .agencyOrigin(jsonObject.optString("agencyOrigin"))
                .uniqueCodeOrigin(jsonObject.optString("uniqueCodeOrigin"))
                .accountDestiny(jsonObject.optString("accountDestiny"))
                .agencyDestiny(jsonObject.optString("agencyDestiny"))
                .build();
    }

}
