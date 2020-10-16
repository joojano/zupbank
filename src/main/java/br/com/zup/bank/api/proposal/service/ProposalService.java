package br.com.zup.bank.api.proposal.service;

import br.com.zup.bank.api.proposal.domain.AbstractProposalOperations;
import br.com.zup.bank.api.proposal.domain.models.Address;
import br.com.zup.bank.api.proposal.domain.models.CPFImage;
import br.com.zup.bank.api.proposal.domain.models.Customer;
import br.com.zup.bank.api.proposal.domain.models.Proposal;
import br.com.zup.bank.api.proposal.domain.enums.StatusApprovalEnum;
import br.com.zup.bank.api.proposal.domain.models.Steps;
import br.com.zup.bank.api.proposal.domain.enums.StepsEnum;
import br.com.zup.bank.api.proposal.repository.ProposalRepository;
import br.com.zup.bank.api.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;

@Service
public class ProposalService implements AbstractProposalOperations {

    private final int LEGAL_AGE = 18;
    
    @Value("${CPF_UPLOAD_DIR}")
    private String cpfDirectory;
    @Autowired
    private ProposalRepository proposalRepository;
    
    @Override
    public ResponseEntity getProposalInfo(String id, String type) {
        
        if (!isProposalExists(id)) return Utils.returnNotFoundMessage();
        Proposal proposal = proposalRepository.findById(id).get();
        if (type.equals("bank")) return ResponseEntity.ok(proposal);
        if (type.equals("client")){
            Proposal clientProposal = Proposal.builder()
                .id(id)
                .address(proposal.getAddress())
                .steps(proposal.getSteps())
                .customer(
                        Customer.builder()
                                .name(proposal.getCustomer().getName())
                                .surname(proposal.getCustomer().getSurname())
                                .email(proposal.getCustomer().getEmail())
                                .birthDate(proposal.getCustomer().getBirthDate())
                                .cpf(proposal.getCustomer().getCpf()).build()).build();
            return ResponseEntity.ok(clientProposal);
        }
        return Utils.returnForbiddenMessage("This token does not have the appropriate roles to see the response");
    }

    @Override
    public ResponseEntity insertAddressInfo(String id, Address address) {
        if (!isCepStringValid(address.getCep())) {
            return Utils.returnBadRequestMessage("CEP is not valid");
        }
        if (!isProposalExists(id)) {
            return Utils.returnNotFoundMessage();
        }

        Proposal originalProposal = proposalRepository.findById(id).get();

        Proposal proposal = Proposal.builder()
                .id(id)
                .customer(originalProposal.getCustomer())
                .address(address)
                .steps(setStepCompleted(originalProposal.getSteps(), StepsEnum.STEP2))
                .build();
        proposalRepository.save(proposal);

        return ResponseEntity.created(URI.create("http://localhost:8080/proposal/" + proposal.getId() + "/uploadCPF")).build();
    }
    @Override
    public ResponseEntity insertCpfFile(String id, MultipartFile image) {
        if (!isFileImage(FilenameUtils.getExtension(image.getOriginalFilename()))) return Utils.returnBadRequestMessage("Filetype not supported. Supported types is .png, .jpe, .jpeg, .heif, .heic");
        if (!isFileImageType(image.getContentType())) return Utils.returnBadRequestMessage("Content type not supported. Supported types is image/png, image/jpeg, image/heic, image/heif");
        if (!isProposalExists(id)) return Utils.returnNotFoundMessage();
        
        Proposal originalProposal = proposalRepository.findById(id).get();
        
        if (!isStepsCompleted(originalProposal.getSteps(), StepsEnum.STEP3)) return Utils.returnUnprocessableEntity("A previous step is not complete.");
        
        String savedPath = saveImageToDisk(originalProposal.getCustomer().getCpf(), cpfDirectory, image);
        if (Objects.isNull(savedPath)) return Utils.returnServerError("There was an error while trying to save the image");
        
        Proposal proposal = Proposal.builder()
                .id(id)
                .address(originalProposal.getAddress())
                .customer(
                        Customer.builder()
                                .name(originalProposal.getCustomer().getName())
                                .surname(originalProposal.getCustomer().getSurname())
                                .email(originalProposal.getCustomer().getEmail())
                                .birthDate(originalProposal.getCustomer().getBirthDate())
                                .cpf(originalProposal.getCustomer().getCpf())
                                .cpfImage(
                                        CPFImage.builder()
                                                .approvalStatus(StatusApprovalEnum.PENDING)
                                                .pathImage(savedPath).build())
                                .build())
                .steps(setStepCompleted(originalProposal.getSteps(), StepsEnum.STEP3)).build();
        
        proposalRepository.save(proposal);
        
        return ResponseEntity.created(URI.create("http://localhost:8080/proposal/" + id)).build();
    }

    @Override
    public ResponseEntity insertCustomerInfo(Customer customer) {
        if (!isEmailStringValid(customer.getEmail())) {
            return Utils.returnBadRequestMessage("Email is not valid");
        }
        if (!isCpfStringValid(customer.getCpf())) {
            return Utils.returnBadRequestMessage("CPF is not valid");
        }
        if (!isClientInLegalAge(customer.getBirthDate())) {
            return Utils.returnBadRequestMessage("Customer is not on legal age");
        }
        if (!isEmailNotRegistered(customer.getEmail())) {
            return Utils.returnBadRequestMessage("Email already registered");
        }
        if (!isCpfNotRegistered(customer.getCpf())) {
            return Utils.returnBadRequestMessage("CPF already registered");
        }

        Proposal proposal = Proposal.builder()
                .customer(customer)
                .steps(setStepCompleted(new Steps(), StepsEnum.STEP1))
                .build();
        proposalRepository.insert(proposal);

        return ResponseEntity.created(URI.create("http://localhost:8080/proposal/" + proposal.getId() + "/customerAddress")).build();
    }
    
    @Override
    public ResponseEntity insertProposalAcceptance(String id, boolean isAccepted, String type) {
        if (!isProposalExists(id)) {
            return Utils.returnNotFoundMessage();
        }
        
        Proposal originalProposal = proposalRepository.findById(id).get();
        
        Steps steps = originalProposal.getSteps();
        if (!isStepsCompleted(steps, StepsEnum.STEP4)) return Utils.returnUnprocessableEntity("A previous step is not complete.");
        
        if(type.equals("bank")) steps = setStepCompleted(steps, StepsEnum.STEP4_BANK, isAccepted);
        else if(type.equals("customer")) steps = setStepCompleted(steps, StepsEnum.STEP4_CUSTOMER, isAccepted);
        else return Utils.returnForbiddenMessage("This token does not have the appropriate roles to set an acceptance");
        
        Proposal proposal = Proposal.builder()
                .id(id)
                .customer(originalProposal.getCustomer())
                .address(originalProposal.getAddress())
                .steps(steps)
                .build();
        
        proposalRepository.save(proposal);
        return ResponseEntity.ok().build();
    }
    

    private boolean isCepStringValid(String cep) {
        String regex = "^\\d{5}-\\d{3}$";
        return Utils.isRegexMatch(regex, cep);
    }
    private boolean isClientInLegalAge(LocalDate birthDate) {
        LocalDate now = LocalDate.now();

        Period period = Period.between(birthDate, now);

        int diffYears = Math.abs(period.getYears());

        if (diffYears >= LEGAL_AGE) {
            return true;
        } else {
            return false;
        }
    }
    private boolean isCpfNotRegistered(String cpf) {
        return proposalRepository.findByCpf(cpf).isEmpty();
    }

    private boolean isCpfStringValid(String cpf) {
        String regex = "(^\\d{3}\\.\\d{3}\\.\\d{3}\\-\\d{2}$)";
        return Utils.isRegexMatch(regex, cpf);
    }


    private boolean isEmailNotRegistered(String email) {
        return proposalRepository.findByEmail(email).isEmpty();
    }
    private boolean isEmailStringValid(String email) {
        String regex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
        return Utils.isRegexMatch(regex, email);
    }


    private boolean isFileImage(String extension) {
        return (extension.equals("png")
                || extension.equals("jpe")
                || extension.equals("jpg")
                || extension.equals("jpeg")
                || extension.equals("heif")
                || extension.equals("heic"));
    }

    private boolean isFileImageType(String contentType) {
        return (contentType.equals("image/jpeg")
                || contentType.equals("image/png")
                || contentType.equals("image/heic")
                || contentType.equals("image/heif"));
    }
    private boolean isProposalExists(String id) {
        return proposalRepository.findById(id).isPresent();
    }

    private boolean isStepsCompleted(Steps steps, StepsEnum actualStep) {
        boolean response = false;
        switch(actualStep) {
            case STEP3:
                response = (steps.isStep1Complete() && steps.isStep2Complete());
                break;
            case STEP4:
                response = (steps.isStep1Complete() && steps.isStep2Complete() && steps.isStep3Complete());
                break;
            default:
                response = false;
        }
        return response;
    }
    

    private String saveImageToDisk(String cpf, String path, MultipartFile file) {
        try {
            String pathToUpload = path.concat("/").concat(cpf).concat("/");
            if (! new File(pathToUpload).exists()){
                new File(pathToUpload).mkdir();
            }          
            String nameFile = Instant.now().toString().concat(".").concat(FilenameUtils.getExtension(file.getOriginalFilename()));
            nameFile = nameFile.replaceAll(":", "");
            String filePath = pathToUpload.concat(nameFile);
            
            File dest = new File(filePath);
            FileUtils.writeByteArrayToFile(dest, file.getBytes());
            return filePath;
        } catch (IOException | IllegalStateException ex) {
            Logger.getLogger(ProposalService.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    private Steps setStepCompleted(Steps step, StepsEnum stepCompleted, boolean isAccepted){
        switch(stepCompleted){
            case STEP1:
                step.setStep1Complete(true);
                break;
            case STEP2:
                step.setStep2Complete(true);
                break;
            case STEP3:
                step.setStep3Complete(true);
                step.setIsAcceptedByBank(StatusApprovalEnum.PENDING);
                step.setIsAcceptedByCustomer(StatusApprovalEnum.PENDING);
                break;
            case STEP4_BANK:
                if (isAccepted) step.setIsAcceptedByBank(StatusApprovalEnum.APPROVED);
                else step.setIsAcceptedByBank(StatusApprovalEnum.REPROVED);
                break;
            case STEP4_CUSTOMER:
                if (isAccepted) step.setIsAcceptedByCustomer(StatusApprovalEnum.APPROVED);
                else step.setIsAcceptedByCustomer(StatusApprovalEnum.REPROVED);
                break;
        }
        return step;
    }
    
    private Steps setStepCompleted(Steps step, StepsEnum stepCompleted) {
        return setStepCompleted(step, stepCompleted, false);
    }
}
