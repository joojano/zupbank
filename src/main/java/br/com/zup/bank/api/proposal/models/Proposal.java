
package br.com.zup.bank.api.proposal.models;

import lombok.Builder;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Document(collection = "proposals")
public class Proposal {
    private String id;
    private Steps steps;
    private Customer customer;
}
