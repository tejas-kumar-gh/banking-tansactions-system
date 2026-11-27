
package com.banking.banking_transaction_system.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.banking.banking_transaction_system.exception.AccountNotFoundException;
import com.banking.banking_transaction_system.exception.InactiveAccountException;
import com.banking.banking_transaction_system.exception.InsufficientBalanceException;
import com.banking.banking_transaction_system.exception.InvalidTransactionException;
import com.banking.banking_transaction_system.model.Account;
import com.banking.banking_transaction_system.model.Transaction;
import com.banking.banking_transaction_system.repository.AccountRepository;
import com.banking.banking_transaction_system.repository.TransactionRepository;

@Service
public class TransactionService {

        private static final Logger logger =
        LoggerFactory.getLogger(TransactionService.class);
        private final FailureRecordingService failureRecordingService;
        private final AccountRepository accountRepository;
        private final TransactionRepository transactionRepository;

      public TransactionService(
        AccountRepository accountRepository,
        TransactionRepository transactionRepository,
        FailureRecordingService failureRecordingService) {

    this.accountRepository = accountRepository;
    this.transactionRepository = transactionRepository;
    this.failureRecordingService = failureRecordingService;
}

       @Transactional
public Account deposit(String accountNumber, BigDecimal amount) {

    String transactionId = UUID.randomUUID().toString();

    try {
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

        accountRepository.updateBalance(account.getAccountId(), newBalance);

        account.setBalance(newBalance);

        transactionRepository.save(
                transactionId,
                null,
                account.getAccountId(),
                amount,
                "DEPOSIT",
                "SUCCESS");

        return account;

    } catch (RuntimeException ex) {

        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            failureRecordingService.recordFailure(
                    transactionId,
                    null,
                    null,
                    amount,
                    "DEPOSIT",
                    ex.getMessage() + " | Account: " + accountNumber,
                    ex.getClass().getSimpleName());
        }

        throw ex;
    }
}

@Transactional
public Account withdraw(String accountNumber, BigDecimal amount) {

    String transactionId = UUID.randomUUID().toString();
    long startTime = System.nanoTime();

    try {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Amount must be greater than zero");
        }

        Account account = accountRepository
                .findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() ->
                        new AccountNotFoundException("Account not found: " + accountNumber));

        if (!"ACTIVE".equals(account.getAccountStatus())) {
            throw new InactiveAccountException("Account is not active");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient account balance");
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);

        accountRepository.updateBalance(account.getAccountId(), newBalance);
        account.setBalance(newBalance);

        transactionRepository.save(
                transactionId,
                account.getAccountId(),
                null,
                amount,
                "WITHDRAWAL",
                "SUCCESS"
        );

        long duration = (System.nanoTime() - startTime) / 1_000_000;

        logger.info(
                "Transaction successful | ID: {} | Type: WITHDRAWAL | Account: {} | Amount: {} | Duration: {} ms | Thread: {}",
                transactionId,
                accountNumber,
                amount,
                duration,
                Thread.currentThread().getName()
        );

        return account;

    } catch (RuntimeException ex) {

        long duration = (System.nanoTime() - startTime) / 1_000_000;

        logger.error(
                "Transaction failed | ID: {} | Type: WITHDRAWAL | Account: {} | Amount: {} | Reason: {} | Duration: {} ms | Thread: {}",
                transactionId,
                accountNumber,
                amount,
                ex.getMessage(),
                duration,
                Thread.currentThread().getName()
        );

        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            failureRecordingService.recordFailure(
                    transactionId,
                    null,
                    null,
                    amount,
                    "WITHDRAWAL",
                    ex.getMessage() + " | Account: " + accountNumber,
                    ex.getClass().getSimpleName()
            );
        }

        throw ex;
    }
}

@Transactional
public Map<String, Account> transfer(
        String fromAccountNumber,
        String toAccountNumber,
        BigDecimal amount) {

    String transactionId = UUID.randomUUID().toString();
    long startTime = System.nanoTime();
    try {
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

        BigDecimal senderNewBalance =
                fromAccount.getBalance().subtract(amount);

        BigDecimal receiverNewBalance =
                toAccount.getBalance().add(amount);

        accountRepository.updateBalance(
                fromAccount.getAccountId(),
                senderNewBalance);

        accountRepository.updateBalance(
                toAccount.getAccountId(),
                receiverNewBalance);

        fromAccount.setBalance(senderNewBalance);
        toAccount.setBalance(receiverNewBalance);

        transactionRepository.save(
                transactionId,
                fromAccount.getAccountId(),
                toAccount.getAccountId(),
                amount,
                "TRANSFER",
                "SUCCESS");

                long duration = (System.nanoTime() - startTime) / 1_000_000;

logger.info(
        "Transaction successful | ID: {} | Type: TRANSFER | From: {} | To: {} | Amount: {} | Duration: {} ms | Thread: {}",
        transactionId,
        fromAccountNumber,
        toAccountNumber,
        amount,
        duration,
        Thread.currentThread().getName()
);

        return Map.of(
                "sender", fromAccount,
                "receiver", toAccount);

    } catch (RuntimeException ex) {

        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                long duration = (System.nanoTime() - startTime) / 1_000_000;

logger.error(
        "Transaction failed | ID: {} | Type: TRANSFER | From: {} | To: {} | Amount: {} | Reason: {} | Duration: {} ms | Thread: {}",
        transactionId,
        fromAccountNumber,
        toAccountNumber,
        amount,
        ex.getMessage(),
        duration,
        Thread.currentThread().getName()
);
            failureRecordingService.recordFailure(
                    transactionId,
                    null,
                    null,
                    amount,
                    "TRANSFER",
                    ex.getMessage()
                            + " | From: " + fromAccountNumber
                            + " | To: " + toAccountNumber,
                    ex.getClass().getSimpleName());
        }

        throw ex;
    }
}

        public List<Transaction> getTransactionHistory(String accountNumber) {

                Account account = accountRepository
                                .findByAccountNumber(accountNumber)
                                .orElseThrow(() -> new AccountNotFoundException(
                                                "Account not found: " + accountNumber));

                return transactionRepository.findByAccountId(account.getAccountId());
        }

}
