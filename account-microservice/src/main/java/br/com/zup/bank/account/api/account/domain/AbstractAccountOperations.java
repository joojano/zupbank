
package br.com.zup.bank.account.api.account.domain;

import br.com.zup.bank.account.api.account.domain.models.FirstAccessModel;
import br.com.zup.bank.account.api.account.domain.models.transfer.Transfer;
import java.util.ArrayList;
import org.springframework.http.ResponseEntity;

public interface AbstractAccountOperations {
    public ResponseEntity createNewAccount(String proposalId);
    public ResponseEntity receiveTransfers(ArrayList<Transfer> transferInfo);
    public ResponseEntity firstAccess(FirstAccessModel firstAccessModel);
}
