
package br.com.zup.bank.account.api.account.domain;

import org.springframework.http.ResponseEntity;

public interface AbstractAccountOperations {
    public ResponseEntity createNewAccount(String proposalId);
}
