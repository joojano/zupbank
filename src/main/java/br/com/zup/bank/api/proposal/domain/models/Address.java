
package br.com.zup.bank.api.proposal.domain.models;

import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.NonNull;

@Builder
@Getter
public class Address {
    @NonNull private String cep;
    @NonNull private String street;
    @NonNull private String neighborhood;
    @NonNull private String complement;
    @NonNull private String city;
    @NonNull private String state;
}
