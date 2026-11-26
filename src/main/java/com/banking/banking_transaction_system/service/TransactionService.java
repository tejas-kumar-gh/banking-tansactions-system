
package com.banking.banking_transaction_system.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banking.banking_transaction_system.exception.AccountNotFoundException;
import com.banking.banking_transaction_system.exception.InactiveAccountException;
import com.banking.banking_transaction_system.exception.InvalidTransactionException;
import com.banking.banking_transaction_system.model.Account;
import com.banking.banking_transaction_system.repository.AccountRepository;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;

    public TransactionService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account deposit(String accountNumber, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException(
                    "Deposit amount must be greater than zero");
        }

        Account account = accountRepository
                .findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found: " + accountNumber));

        if (!"ACTIVE".equals(account.getAccountStatus())) {
            throw new InactiveAccountException(
                    "Account is not active: " + accountNumber);
        }

        BigDecimal newBalance = account.getBalance().add(amount);

        accountRepository.updateBalance(
                account.getAccountId(),
                newBalance);

        account.setBalance(newBalance);

        return account;
    }

}
