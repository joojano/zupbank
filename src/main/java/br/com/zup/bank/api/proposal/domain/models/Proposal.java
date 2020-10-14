package br.com.zup.bank.api.proposal.domain.models;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Getter
@Document(collection = "proposals")
public class Proposal {
    private String id;
    private Steps steps;
    private Customer customer;
    private Address address;
}
