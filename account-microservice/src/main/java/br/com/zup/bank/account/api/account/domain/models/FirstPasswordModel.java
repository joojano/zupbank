
package br.com.zup.bank.account.api.account.domain.models;

import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.NonNull;

@Builder
@Getter
public class FirstPasswordModel {
   @NonNull private String token;
   @NonNull private String password;
}
