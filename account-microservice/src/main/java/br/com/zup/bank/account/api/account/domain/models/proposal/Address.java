
package br.com.zup.bank.account.api.account.domain.models.proposal;

import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Builder
@Getter
public class Address {
    @NonNull private String cep;
    @NonNull private String street;
    @Nullable private String number;
    @NonNull private String neighborhood;
    @Nullable private String complement;
    @NonNull private String city;
    @NonNull private String state;
}
