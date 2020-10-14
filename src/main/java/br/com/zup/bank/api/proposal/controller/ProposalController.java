
package br.com.zup.bank.api.proposal.controller;

import br.com.zup.bank.api.proposal.domain.models.Customer;
import br.com.zup.bank.api.proposal.domain.AbstractProposalOperations;
import br.com.zup.bank.api.proposal.service.ProposalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProposalController implements AbstractProposalOperations {
    
    
    @Autowired
    private ProposalService proposalService;

    @Operation(summary = "Check, insert customer info in database and create a new Proposal (step 1)", 
            description = "This endpoint will receive customer basic informations. Based on that, the provided information"
                    + " will be checked. If everything is ok, the registration may proceed")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operation successful"),
        @ApiResponse(responseCode = "204", description = "Invalid parameter or already registered", content = @Content(mediaType = "application/json"))
    })
    
    @PostMapping(
    path = "/proposal/clientInfo",
    consumes = "application/json")
    public ResponseEntity insertCustomerInfo(@Parameter(description = "The information of customer") @RequestBody Customer customer) {
        return proposalService.insertCustomerInfo(customer);
    }

}
