
package com.banking.banking_transaction_system.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banking.banking_transaction_system.exception.AccountNotFoundException;
import com.banking.banking_transaction_system.exception.InactiveAccountException;
import com.banking.banking_transaction_system.exception.InsufficientBalanceException;
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

    @Transactional
    public Account withdraw(String accountNumber, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException(
                    "Withdrawal amount must be greater than zero");
        }

        Account account = accountRepository
                .findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found: " + accountNumber));

        if (!"ACTIVE".equals(account.getAccountStatus())) {
            throw new InactiveAccountException(
                    "Account is not active: " + accountNumber);
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Available balance: "
                            + account.getBalance());
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);

        accountRepository.updateBalance(account.getAccountId(), newBalance);

        account.setBalance(newBalance);

        return account;
    }

    @Transactional
    public Map<String, Account> transfer(
            String fromAccountNumber,
            String toAccountNumber,
            BigDecimal amount) {

        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new InvalidTransactionException(
                    "Source and destination accounts cannot be the same");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException(
                    "Transfer amount must be greater than zero");
        }

        List<Account> accounts = accountRepository.findAccountsForUpdate(
                fromAccountNumber,
                toAccountNumber);

        if (accounts.size() != 2) {
            throw new AccountNotFoundException(
                    "Source or destination account not found");
        }

        Account fromAccount = accounts.stream()
                .filter(a -> a.getAccountNumber().equals(fromAccountNumber))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException(
                        "Source account not found: " + fromAccountNumber));

        Account toAccount = accounts.stream()
                .filter(a -> a.getAccountNumber().equals(toAccountNumber))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException(
                        "Destination account not found: " + toAccountNumber));

        if (!"ACTIVE".equals(fromAccount.getAccountStatus())) {
            throw new InactiveAccountException(
                    "Source account is not active");
        }

        if (!"ACTIVE".equals(toAccount.getAccountStatus())) {
            throw new InactiveAccountException(
                    "Destination account is not active");
        }

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance in source account");
        }

        BigDecimal senderNewBalance = fromAccount.getBalance().subtract(amount);

        BigDecimal receiverNewBalance = toAccount.getBalance().add(amount);

        accountRepository.updateBalance(
                fromAccount.getAccountId(),
                senderNewBalance);

        accountRepository.updateBalance(
                toAccount.getAccountId(),
                receiverNewBalance);

        fromAccount.setBalance(senderNewBalance);
        toAccount.setBalance(receiverNewBalance);

        return Map.of(
                "sender", fromAccount,
                "receiver", toAccount);
    }

}
