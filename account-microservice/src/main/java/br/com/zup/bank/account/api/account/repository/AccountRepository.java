
package br.com.zup.bank.account.api.account.repository;

import br.com.zup.bank.account.api.account.domain.models.Account;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccountRepository extends MongoRepository<Account, String>{

}
