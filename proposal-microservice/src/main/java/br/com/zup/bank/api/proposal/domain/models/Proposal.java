package br.com.zup.bank.api.proposal.domain.models;

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
