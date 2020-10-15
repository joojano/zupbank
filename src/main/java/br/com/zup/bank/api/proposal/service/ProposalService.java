package br.com.zup.bank.api.proposal.service;

import br.com.zup.bank.api.proposal.domain.AbstractProposalOperations;
import br.com.zup.bank.api.proposal.domain.models.Address;
import br.com.zup.bank.api.proposal.domain.models.CPFImage;
import br.com.zup.bank.api.proposal.domain.models.Customer;
import br.com.zup.bank.api.proposal.domain.models.Proposal;
import br.com.zup.bank.api.proposal.domain.models.StatusApprovalEnum;
import br.com.zup.bank.api.proposal.domain.models.Steps;
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
import org.springframework.http.HttpStatus;

@Service
public class ProposalService implements AbstractProposalOperations {

    @Autowired
    private ProposalRepository proposalRepository;

    private final int LEGAL_AGE = 18;
    private final int STEP3 = 3;
    
    @Value("${CPF_UPLOAD_DIR}")
    private String cpfDirectory;
    
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
                .steps(
                        Steps.builder()
                                .isStep1Complete(true)
                                .isStep2Complete(true)
                                .build())
                .build();
        proposalRepository.save(proposal);

        return ResponseEntity.created(URI.create("http://localhost:8080/proposal/" + proposal.getId() + "/uploadCPF")).build();
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
                .steps(
                        Steps.builder()
                                .isStep1Complete(true)
                                .build())
                .build();
        proposalRepository.insert(proposal);

        return ResponseEntity.created(URI.create("http://localhost:8080/proposal/" + proposal.getId() + "/customerAddress")).build();
    }
    
    @Override
    public ResponseEntity insertCpfFile(String id, MultipartFile image) {
            if (!isFileImage(FilenameUtils.getExtension(image.getOriginalFilename()))) return Utils.returnBadRequestMessage("Filetype not supported. Supported types is .png, .jpe, .jpeg, .heif, .heic");
            if (!isFileImageType(image.getContentType())) return Utils.returnBadRequestMessage("Content type not supported. Supported types is image/png, image/jpeg, image/heic, image/heif");
            if (!isProposalExists(id)) return Utils.returnNotFoundMessage();
            
            Proposal originalProposal = proposalRepository.findById(id).get();
            
            if (!isStepsCompleted(originalProposal.getSteps(), STEP3)) return Utils.returnUnprocessableEntity("A previous step is not complete.");
            
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
                    .steps(
                            Steps.builder()
                                    .isStep1Complete(true)
                                    .isStep2Complete(true)
                                    .isStep3Complete(true).build()).build();
            
            proposalRepository.save(proposal);     
            
            return ResponseEntity.created(URI.create("http://localhost:8080/proposal/" + id)).build();
    }

    private boolean isCepStringValid(String cep) {
        String regex = "^\\d{5}-\\d{3}$";
        return Utils.isRegexMatch(regex, cep);
    }

    private boolean isEmailStringValid(String email) {
        String regex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
        return Utils.isRegexMatch(regex, email);
    }

    private boolean isCpfStringValid(String cpf) {
        String regex = "(^\\d{3}\\.\\d{3}\\.\\d{3}\\-\\d{2}$)";
        return Utils.isRegexMatch(regex, cpf);
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

    private boolean isEmailNotRegistered(String email) {
        return proposalRepository.findByEmail(email).isEmpty();
    }

    private boolean isCpfNotRegistered(String cpf) {
        return proposalRepository.findByCpf(cpf).isEmpty();
    }

    private boolean isProposalExists(String id) {
        return proposalRepository.findById(id).isPresent();
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

    private boolean isStepsCompleted(Steps steps, int actualStep) {
        boolean response = false;
        switch(actualStep) {
            case STEP3:
                response = (steps.isStep1Complete() && steps.isStep2Complete());
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

    @Override
    public ResponseEntity insertProposalAcceptance(String id, boolean isAcceptedByCustomer, String type) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
