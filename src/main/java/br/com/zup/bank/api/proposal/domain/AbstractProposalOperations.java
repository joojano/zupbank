package br.com.zup.bank.api.proposal.domain;

import br.com.zup.bank.api.proposal.domain.models.Address;
import br.com.zup.bank.api.proposal.domain.models.Customer;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author jrmgalli
 */
public interface AbstractProposalOperations {
    public ResponseEntity insertCustomerInfo(Customer customer);
    public ResponseEntity insertAddressInfo(String id, Address address);
    public ResponseEntity insertCpfFile(String id, MultipartFile image);
    public ResponseEntity getProposalInfo(String id, Authentication auth);
    public ResponseEntity insertProposalAcceptance(String id, boolean isAccepted, Authentication auth);
}
