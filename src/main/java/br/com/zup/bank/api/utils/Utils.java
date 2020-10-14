
package br.com.zup.bank.api.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.ResponseEntity;

public class Utils {
    
    public static boolean isRegexMatch(String regex, String wordToBeMatched){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(wordToBeMatched);
        return matcher.matches();
    }
    
    public static ResponseEntity returnBadRequestMessage(String message){
        return ResponseEntity.badRequest().body(createMessageBody(message));
    }
    
    public static ResponseEntity returnNotFoundMessage(){
        return ResponseEntity.notFound().build();
    }
    
    private static Map<String, Object> createMessageBody(String message){
        Map<String, Object> body = new HashMap();
        body.put("message", message);
        return body;
    }
}
