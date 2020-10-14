
package br.com.zup.bank.api.proposal.repository;

import br.com.zup.bank.api.proposal.domain.models.Proposal;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ProposalRepository extends MongoRepository<Proposal, String>{
    @Query("{ 'customer.cpf' : ?0 }")
    List<Proposal> findByCpf(String cpf);
    
    @Query("{ 'customer.email' : ?0 }")
    List<Proposal> findByEmail(String email);
}
