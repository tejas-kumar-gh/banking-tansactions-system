
package com.banking.banking_transaction_system.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banking.banking_transaction_system.model.Account;
import com.banking.banking_transaction_system.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<Account> deposit(
            @RequestBody Map<String, String> request) {

        String accountNumber = request.get("accountNumber");
        BigDecimal amount = new BigDecimal(request.get("amount"));

        Account updatedAccount = transactionService.deposit(
                accountNumber, amount
        );

        return ResponseEntity.ok(updatedAccount);
    }
}
