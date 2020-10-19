
package br.com.zup.bank.account.api.account.repository;

import br.com.zup.bank.account.api.account.domain.models.Account;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface AccountRepository extends MongoRepository<Account, String>{

    @Query("{ 'proposalId' : ?0 }")
    public Optional<Account> findByProposalId(String proposalId);
    
    @Query("{ 'agencyNumber' : ?0, 'accountNumber' : ?1 }")
    public Optional<Account> findAccountByAgencyAndNumber(String agencyNumber, String accountNumber);

}
