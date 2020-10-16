
package br.com.zup.bank.api.proposal.domain.models;

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
