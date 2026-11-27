
package com.banking.banking_transaction_system.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banking.banking_transaction_system.model.Account;
import com.banking.banking_transaction_system.model.Transaction;
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
        accountNumber, amount);

    return ResponseEntity.ok(updatedAccount);
  }

  @PostMapping("/withdraw")
  public ResponseEntity<Account> withdraw(
      @RequestBody Map<String, String> request) {

    String accountNumber = request.get("accountNumber");
    BigDecimal amount = new BigDecimal(request.get("amount"));

    Account account = transactionService.withdraw(accountNumber, amount);

    return ResponseEntity.ok(account);
  }

  @PostMapping("/transfer")
  public ResponseEntity<Map<String, Account>> transfer(
      @RequestBody Map<String, String> request) {

    String fromAccountNumber = request.get("fromAccountNumber");
    String toAccountNumber = request.get("toAccountNumber");
    BigDecimal amount = new BigDecimal(request.get("amount"));

    Map<String, Account> result = transactionService.transfer(
        fromAccountNumber,
        toAccountNumber,
        amount);

    return ResponseEntity.ok(result);
  }

  @GetMapping("/history/{accountNumber}")
  public ResponseEntity<List<Transaction>> getTransactionHistory(
      @PathVariable String accountNumber) {

    List<Transaction> transactions = transactionService.getTransactionHistory(accountNumber);

    return ResponseEntity.ok(transactions);
  }
}
