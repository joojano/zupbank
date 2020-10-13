package br.com.zup.bank.api.proposal.controller;

import br.com.zup.bank.api.proposal.models.Customer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface AbstractProposalController {
    
    public ResponseEntity insertClientInfo(@RequestBody Customer customer);
}
