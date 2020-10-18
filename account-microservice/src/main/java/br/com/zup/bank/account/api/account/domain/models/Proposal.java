package br.com.zup.bank.account.api.account.domain.models;

import br.com.zup.bank.account.api.account.domain.models.proposal.Address;
import br.com.zup.bank.account.api.account.domain.models.proposal.Customer;
import br.com.zup.bank.account.api.account.domain.models.proposal.Steps;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

@Builder
@Getter
@Document(collection = "proposals")
public class Proposal {
    @Id
    private String id;
    @Nullable private Steps steps;
    @Nullable private Customer customer;
    @Nullable private Address address;
}
