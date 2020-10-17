
package br.com.zup.bank.account.api.account.domain.models;

import java.time.LocalDate;
import javax.validation.constraints.Email;
import lombok.Builder;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Builder
@Getter
public class Customer {
    
    @NonNull private String name;
    @NonNull private String surname;
    @NonNull @Email private String email;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NonNull private LocalDate birthDate;
    @NonNull private String cpf;
    @Nullable private CPFImage cpfImage;    
}
