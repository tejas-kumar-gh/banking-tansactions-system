
package com.banking.banking_transaction_system.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banking.banking_transaction_system.model.Account;
import com.banking.banking_transaction_system.service.AccountService;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(
            @RequestBody Map<String, Object> request) {

        Long customerId = Long.valueOf(
                request.get("customerId").toString()
        );

        BigDecimal initialBalance = new BigDecimal(
                request.get("initialBalance").toString()
        );

        Account account = accountService.createAccount(
                customerId, initialBalance
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }
}
