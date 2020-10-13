
package br.com.zup.bank.api.proposal.repository;

import br.com.zup.bank.api.proposal.models.Proposal;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProposalRepository extends MongoRepository<Proposal, String>{

}
