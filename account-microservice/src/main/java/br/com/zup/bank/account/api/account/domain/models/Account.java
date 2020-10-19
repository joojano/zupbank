
package br.com.zup.bank.account.api.account.domain.models;

import java.math.BigDecimal;
import javax.validation.constraints.Null;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.lang.NonNull;

@Builder
@Getter
public class Account {
    @Id private String id;
    @NonNull private String agencyNumber;
    @NonNull private String accountNumber;
    @NonNull private String bankNumber;
    @Null private String password;
    @Null private Token token;
    @NonNull @Setter private BigDecimal balance;
    @NonNull @Indexed(unique = true) private String proposalId;
}
