
package br.com.zup.bank.api.proposal.controller;

import br.com.zup.bank.api.proposal.models.Customer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProposalController implements AbstractProposalController {

    @PostMapping(
    path = "/proposal/clientInfo",
    consumes = "application/json")
    public ResponseEntity insertClientInfo(Customer customer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
