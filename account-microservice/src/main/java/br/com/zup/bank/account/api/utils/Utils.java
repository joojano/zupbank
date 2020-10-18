
package br.com.zup.bank.account.api.utils;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class Utils {
    
    private static Map<String, Object> createMessageBody(String message){
        Map<String, Object> body = new HashMap();
        body.put("message", message);
        return body;
    }
    
    public static ResponseEntity returnBadRequestMessage(String message){
        return ResponseEntity.badRequest().body(createMessageBody(message));
    }

    public static ResponseEntity returnConflict(String message) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(createMessageBody(message));
    }
    
    public static ResponseEntity returnNotFoundMessage(){
        return ResponseEntity.notFound().build();
    }
    
    public static ResponseEntity returnServerError(String message){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createMessageBody(message));
    }

    public static ResponseEntity returnUnprocessableEntity(String message) {
        return ResponseEntity.unprocessableEntity().body(createMessageBody(message));
    }

    public static ResponseEntity returnForbiddenMessage(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(createMessageBody(message));
    }
}
