package br.com.zup.bank.api.proposal.domain;

import br.com.zup.bank.api.proposal.domain.models.Address;
import br.com.zup.bank.api.proposal.domain.models.Customer;
import org.springframework.http.ResponseEntity;

/**
 *
 * @author jrmgalli
 */
public interface AbstractProposalOperations {
    public ResponseEntity insertCustomerInfo(Customer customer);
    public ResponseEntity insertAddressInfo(String id, Address address);
}
