
package br.com.zup.bank.account.api.account.controller;

import br.com.zup.bank.account.api.account.domain.AbstractAccountOperations;
import br.com.zup.bank.account.api.account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController implements AbstractAccountOperations {
    
    @Autowired
    private AccountService accountService;

    @PostMapping(path = "/account", consumes = "application/json")
    @Override
    public ResponseEntity createNewAccount(@RequestParam(name = "proposalId") String proposalId) {
        return accountService.createNewAccount(proposalId);
    }

}
