
package br.com.zup.bank.api.proposal.controller;

import br.com.zup.bank.api.proposal.domain.models.Customer;
import br.com.zup.bank.api.proposal.domain.AbstractProposalOperations;
import br.com.zup.bank.api.proposal.domain.models.Address;
import br.com.zup.bank.api.proposal.service.ProposalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Tag(name = "proposal")
@RequestMapping("/proposal")
public class ProposalController implements AbstractProposalOperations {
        
    @Autowired
    private ProposalService proposalService;

    @Operation(summary = "Check, insert customer info in database and create a new Proposal (step 1)",
            description = "This endpoint will receive customer basic informations. Based on that, the provided information"
                    + " will be checked. If everything is ok, the registration may proceed")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Operation successful"),
        @ApiResponse(responseCode = "400", description = "Invalid parameter or already registered", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Provided token is invalid", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "Provided token is not authorized to access this content", content = @Content(mediaType = "application/json"))
    })
    
    @PostMapping(
            path = "/customerInfo", 
            consumes = "application/json")
    @Override
    public ResponseEntity insertCustomerInfo(@Parameter(description = "The information of the customer") @RequestBody Customer customer) {
        return proposalService.insertCustomerInfo(customer);
    }

    @Operation(summary = "Updates a Proposal with the provided address (Step 2)", 
            description = "This endpoint will check if the proposal exists and validate the data. "
                    + "If everything is ok, the registration may proceed")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Operation successful"),
        @ApiResponse(responseCode = "400", description = "Invalid parameter"),
        @ApiResponse(responseCode = "401", description = "Provided token is invalid", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "Provided token is not authorized to access this content", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Proposal not completed")
    })
    @PatchMapping(
            path = "/{id}/customerAddress", 
            consumes = "application/json")
    @Override
    public ResponseEntity insertAddressInfo(@Parameter(description = "The Proposal ID") @PathVariable String id, 
            @Parameter(description = "The address of the customer") @RequestBody Address address) {
        return proposalService.insertAddressInfo(id, address);
    }
    
    @Operation(
            summary = "Updates a Proposal and insert the customer CPF image file (Step 3)", 
            description = "This endpoint will receive an image file of the customer CPF, and verify if the previous steps (step 1 and 2) are done."
                    + "If everything is ok, the registration may proceed. Supported file extensions: .png, .jpe, .jpg, .jpeg, .heif, .heic")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Operation successful"),
        @ApiResponse(responseCode = "400", description = "Invalid parameter"),
        @ApiResponse(responseCode = "401", description = "Provided token is invalid", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "Provided token is not authorized to access this content", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Proposal not found"),
        @ApiResponse(responseCode = "422", description = "Previous steps not completed")
    })
    @PatchMapping(
            path = "/{id}/uploadCPF")
    @Override
    public ResponseEntity insertCpfFile(@Parameter(description = "The Proposal Id") @PathVariable String id, 
            @Parameter(description = "The CPF image") @RequestParam("file") MultipartFile image) {
        return proposalService.insertCpfFile(id, image);
    }
    
    @Operation(
            summary = "Get a proposal", 
            description = "This endpoint will return an proposal based on params provided. "
                    + "If request token has the role 'zupbank-client', some results will be omitted for security")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Information of customer"),
        @ApiResponse(responseCode = "401", description = "Provided token is invalid", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "Provided token is not authorized to access this content", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Proposal not found")
    })
    @GetMapping( 
            produces = "application/json")
    @Override
    public ResponseEntity getProposalInfo(@Parameter(description = "The Proposal Id") @RequestParam(required = false) String id,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String email,
            Authentication auth) {
        return proposalService.getProposalInfo(id, cpf, email, auth);
    }
    
    @Operation(
            summary = "Insert acceptance of a proposal (step 4)", 
            description = "This endpoint will register the acceptance option of the bank and of the customer."
                    + "The accpetance will be registered based on token role ('zupbank-client' or 'zupbank-approver')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Message based on acceptance option"),
        @ApiResponse(responseCode = "401", description = "Provided token is invalid", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "Provided token is not authorized to access this content", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Proposal not found"),
        @ApiResponse(responseCode = "422", description = "Previous steps not completed")
    })
    @PatchMapping(path = "/{id}/acceptance", 
            consumes = "application/json", 
            produces = "application/json")
    @Override
    public ResponseEntity insertProposalAcceptance(@Parameter(description = "The Proposal Id") @PathVariable String id, 
            @Parameter(description = "The acceptance decision") @RequestBody boolean isAccepted, Authentication auth) {
        return proposalService.insertProposalAcceptance(id, isAccepted, auth);
    }
}
