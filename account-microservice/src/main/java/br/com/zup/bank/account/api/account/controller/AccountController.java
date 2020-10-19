
package br.com.zup.bank.account.api.account.controller;

import br.com.zup.bank.account.api.account.domain.AbstractAccountOperations;
import br.com.zup.bank.account.api.account.domain.models.transfer.GetTransferModel;
import br.com.zup.bank.account.api.account.domain.models.transfer.Transfer;
import br.com.zup.bank.account.api.account.service.AccountService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController implements AbstractAccountOperations {
    
    @Autowired
    private AccountService accountService;
    
    @GetMapping(path = "/account/transfer")
    @Override
    public ResponseEntity getTransfer(GetTransferModel transferModel) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @PostMapping(path = "/account", consumes = "application/json")
    @Override
    public ResponseEntity createNewAccount(@RequestParam(name = "proposalId") String proposalId) {
        return accountService.createNewAccount(proposalId);
    }
    
    @PostMapping(path = "/account/transfer/in", consumes = "application/json")
    @Override
    public ResponseEntity receiveTransfers(@RequestBody ArrayList<Transfer> transferInfo) {
        return accountService.receiveTransfers(transferInfo);
    }

    
}
