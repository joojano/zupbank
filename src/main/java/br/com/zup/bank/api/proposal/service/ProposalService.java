package br.com.zup.bank.api.proposal.service;

import br.com.zup.bank.api.proposal.domain.AbstractProposalOperations;
import br.com.zup.bank.api.proposal.domain.models.Address;
import br.com.zup.bank.api.proposal.domain.models.Customer;
import br.com.zup.bank.api.proposal.domain.models.Proposal;
import br.com.zup.bank.api.proposal.domain.models.Steps;
import br.com.zup.bank.api.proposal.repository.ProposalRepository;
import br.com.zup.bank.api.utils.Utils;
import java.net.URI;
import java.time.LocalDate;
import java.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ProposalService implements AbstractProposalOperations {
    
    @Autowired
    private ProposalRepository proposalRepository;
    
    private final int LEGAL_AGE = 18;

    @Override
    public ResponseEntity insertAddressInfo(String id, Address address) {
        if (!isCepStringValid(address.getCep())) return Utils.returnBadRequestMessage("CEP is not valid");
        if (!isProposalExists(id)) return Utils.returnNotFoundMessage();
        
        Proposal originalProposal = proposalRepository.findById(id).get();
                
        Proposal proposal = Proposal.builder()
                .id(id)
                .customer(originalProposal.getCustomer())
                .address(address)
                .steps(
                        Steps.builder()
                                .isStep1Complete(true)
                                .isStep2Complete(true)
                                .isStep3Complete(false)
                                .isAccepted(false)
                                .build())
                .build()
                ;
        proposalRepository.save(proposal);
        
        return ResponseEntity.created(URI.create("http://localhost:8080/proposal/" + proposal.getId() + "/uploadCPF")).build();
    }
    
    @Override
    public ResponseEntity insertCustomerInfo(Customer customer) {
        if (!isEmailStringValid(customer.getEmail())) return Utils.returnBadRequestMessage("Email is not valid");
        if (!isCpfStringValid(customer.getCpf())) return Utils.returnBadRequestMessage("CPF is not valid");
        if (!isClientInLegalAge(customer.getBirthDate())) return Utils.returnBadRequestMessage("Customer is not on legal age");
        if (!isEmailNotRegistered(customer.getEmail())) return Utils.returnBadRequestMessage("Email already registered");
        if (!isCpfNotRegistered(customer.getCpf())) return Utils.returnBadRequestMessage("CPF already registered");
        
        Proposal proposal = Proposal.builder()
                .customer(customer)
                .steps(
                        Steps.builder()
                                .isStep1Complete(true)
                                .isStep2Complete(false)
                                .isStep3Complete(false)
                                .isAccepted(false)
                                .build())
                .build();
        proposalRepository.insert(proposal);
        
        return ResponseEntity.created(URI.create("http://localhost:8080/proposal/" + proposal.getId() + "/customerAddress")).build();
    }
    
    private boolean isCepStringValid(String cep) {
        String regex = "^\\d{5}-\\d{3}$";
        return Utils.isRegexMatch(regex, cep);
    }
    
    private boolean isEmailStringValid(String email){
        String regex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
        return Utils.isRegexMatch(regex, email);
    }
    
    private boolean isCpfStringValid(String cpf){
        String regex = "(^\\d{3}\\.\\d{3}\\.\\d{3}\\-\\d{2}$)";
        return Utils.isRegexMatch(regex, cpf);
    }
    
    private boolean isClientInLegalAge(LocalDate birthDate){
       LocalDate now = LocalDate.now();
       
       Period period = Period.between(birthDate, now);
       
       int diffYears = Math.abs(period.getYears());
       
       if (diffYears >= LEGAL_AGE){
            return true;
       } else {
           return false;
       }
   }
    
    private boolean isEmailNotRegistered(String email){
        return proposalRepository.findByEmail(email).isEmpty();
    }
    
    private boolean isCpfNotRegistered(String cpf){
        return proposalRepository.findByCpf(cpf).isEmpty();
    }

    private boolean isProposalExists(String id) {
        return proposalRepository.findById(id).isPresent();
    }

}
