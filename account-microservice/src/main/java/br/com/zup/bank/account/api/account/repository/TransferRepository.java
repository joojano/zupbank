
package br.com.zup.bank.account.api.account.repository;

import br.com.zup.bank.account.api.account.domain.models.transfer.Transfer;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface TransferRepository extends MongoRepository<Transfer, String>{
    @Query("{ 'bankOrigin' : ?0, 'uniqueCodeOrigin' : ?1 }")
    public Optional<Transfer> findSameTransferByBank(String bankOrigin, String uniqueCodeOrigin);
}
