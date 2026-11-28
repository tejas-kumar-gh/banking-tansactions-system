
package com.banking.banking_transaction_system.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banking.banking_transaction_system.exception.AccountNotFoundException;
import com.banking.banking_transaction_system.exception.DuplicateTransactionException;
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

private void validateIdempotencyKey(String idempotencyKey) {

    if (idempotencyKey == null || idempotencyKey.isBlank()) {
        throw new InvalidTransactionException(
                "Idempotency key is required");
    }

    if (idempotencyKey.length() > 100) {
        throw new InvalidTransactionException(
                "Idempotency key cannot exceed 100 characters");
    }

    if (transactionRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
        throw new DuplicateTransactionException(
                "Duplicate transaction request: " + idempotencyKey);
    }
}

private void savePendingTransaction(
        String transactionId,
        String idempotencyKey,
        Long sourceAccountId,
        Long destinationAccountId,
        BigDecimal amount,
        String transactionType) {

    try {
        transactionRepository.save(
                transactionId,
                idempotencyKey,
                sourceAccountId,
                destinationAccountId,
                amount,
                transactionType,
                "PENDING");

    } catch (DuplicateKeyException ex) {
        throw new DuplicateTransactionException(
                "This transaction request has already been processed");
    }
}

@Transactional
public Account deposit(
        String accountNumber,
        BigDecimal amount,
        String idempotencyKey) {

    String transactionId = UUID.randomUUID().toString();

    try {
        validateIdempotencyKey(idempotencyKey);

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

        savePendingTransaction(
                transactionId,
                idempotencyKey,
                null,
                account.getAccountId(),
                amount,
                "DEPOSIT");

        BigDecimal newBalance = account.getBalance().add(amount);

        accountRepository.updateBalance(
                account.getAccountId(), newBalance);

        transactionRepository.updateStatus(transactionId, "SUCCESS");

        account.setBalance(newBalance);

        logger.info(
                "Transaction SUCCESS | transactionId={} | type=DEPOSIT | account={} | amount={} | thread={}",
                transactionId, accountNumber, amount,
                Thread.currentThread().getName());

        return account;

    } catch (DuplicateTransactionException ex) {
        throw ex;

    } catch (RuntimeException ex) {

        logger.error(
                "Transaction FAILED | transactionId={} | type=DEPOSIT | account={} | reason={}",
                transactionId, accountNumber, ex.getMessage());

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
public Account withdraw(
        String accountNumber,
        BigDecimal amount,
        String idempotencyKey) {

    String transactionId = UUID.randomUUID().toString();

    try {
        validateIdempotencyKey(idempotencyKey);

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
                    "Insufficient account balance");
        }

        savePendingTransaction(
                transactionId,
                idempotencyKey,
                account.getAccountId(),
                null,
                amount,
                "WITHDRAWAL");

        BigDecimal newBalance = account.getBalance().subtract(amount);

        accountRepository.updateBalance(
                account.getAccountId(), newBalance);

        transactionRepository.updateStatus(transactionId, "SUCCESS");

        account.setBalance(newBalance);

        logger.info(
                "Transaction SUCCESS | transactionId={} | type=WITHDRAWAL | account={} | amount={} | thread={}",
                transactionId, accountNumber, amount,
                Thread.currentThread().getName());

        return account;

    } catch (DuplicateTransactionException ex) {
        throw ex;

    } catch (RuntimeException ex) {

        logger.error(
                "Transaction FAILED | transactionId={} | type=WITHDRAWAL | account={} | reason={}",
                transactionId, accountNumber, ex.getMessage());

        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            failureRecordingService.recordFailure(
                    transactionId,
                    null,
                    null,
                    amount,
                    "WITHDRAWAL",
                    ex.getMessage() + " | Account: " + accountNumber,
                    ex.getClass().getSimpleName());
        }

        throw ex;
    }
}

@Transactional
public Map<String, Account> transfer(
        String fromAccountNumber,
        String toAccountNumber,
        BigDecimal amount,
        String idempotencyKey) {

    String transactionId = UUID.randomUUID().toString();

    try {
        validateIdempotencyKey(idempotencyKey);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException(
                    "Transfer amount must be greater than zero");
        }

        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new InvalidTransactionException(
                    "Source and destination accounts cannot be the same");
        }

        List<Account> accounts = accountRepository.findAccountsForUpdate(
                fromAccountNumber, toAccountNumber);

        if (accounts.size() != 2) {
            throw new AccountNotFoundException(
                    "Source or destination account not found");
        }

        Account sender = accounts.stream()
                .filter(a -> a.getAccountNumber().equals(fromAccountNumber))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException(
                        "Source account not found"));

        Account receiver = accounts.stream()
                .filter(a -> a.getAccountNumber().equals(toAccountNumber))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException(
                        "Destination account not found"));

        if (!"ACTIVE".equals(sender.getAccountStatus())
                || !"ACTIVE".equals(receiver.getAccountStatus())) {
            throw new InactiveAccountException(
                    "Both accounts must be active");
        }

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance in source account");
        }

        savePendingTransaction(
                transactionId,
                idempotencyKey,
                sender.getAccountId(),
                receiver.getAccountId(),
                amount,
                "TRANSFER");

        BigDecimal senderBalance = sender.getBalance().subtract(amount);
        BigDecimal receiverBalance = receiver.getBalance().add(amount);

        accountRepository.updateBalance(
                sender.getAccountId(), senderBalance);

        accountRepository.updateBalance(
                receiver.getAccountId(), receiverBalance);

        transactionRepository.updateStatus(transactionId, "SUCCESS");

        sender.setBalance(senderBalance);
        receiver.setBalance(receiverBalance);

        logger.info(
                "Transaction SUCCESS | transactionId={} | type=TRANSFER | from={} | to={} | amount={} | thread={}",
                transactionId, fromAccountNumber, toAccountNumber,
                amount, Thread.currentThread().getName());

        return Map.of("sender", sender, "receiver", receiver);

    } catch (DuplicateTransactionException ex) {
        throw ex;

    } catch (RuntimeException ex) {

        logger.error(
                "Transaction FAILED | transactionId={} | type=TRANSFER | from={} | to={} | reason={}",
                transactionId, fromAccountNumber, toAccountNumber,
                ex.getMessage());

        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            failureRecordingService.recordFailure(
                    transactionId,
                    null,
                    null,
                    amount,
                    "TRANSFER",
                    ex.getMessage() + " | From: " + fromAccountNumber
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
