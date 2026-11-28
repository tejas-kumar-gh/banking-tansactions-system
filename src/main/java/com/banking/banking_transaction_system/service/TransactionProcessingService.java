
package com.banking.banking_transaction_system.service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.banking.banking_transaction_system.model.Account;
import com.banking.banking_transaction_system.exception.InsufficientBalanceException;

@Service
public class TransactionProcessingService {

    private final ExecutorService transactionExecutor;
    private final TransactionService transactionService;
    private final PerformanceMetricsCollector metricsCollector;

    public TransactionProcessingService(
            @Qualifier("transactionExecutor") ExecutorService transactionExecutor,
            TransactionService transactionService,
            PerformanceMetricsCollector metricsCollector) {

        this.transactionExecutor = transactionExecutor;
        this.transactionService = transactionService;
        this.metricsCollector = metricsCollector;
    }


private <T> CompletableFuture<T> submit(Supplier<T> task) {

    metricsCollector.recordSubmitted();

    return CompletableFuture.supplyAsync(() -> {

        long startTime = System.nanoTime();

        metricsCollector.recordResourceUsage();

        try {
            T result = task.get();

            long duration = System.nanoTime() - startTime;
            metricsCollector.recordSuccess(duration);

            return result;

        } catch (RuntimeException ex) {

            long duration = System.nanoTime() - startTime;
            String exceptionName = ex.getClass().getSimpleName();

            if (exceptionName.equals("InsufficientBalanceException")) {
                metricsCollector.recordBusinessFailure(duration);

            } else if (
                    exceptionName.equals("InvalidTransactionException")
                    || exceptionName.equals("AccountNotFoundException")
                    || exceptionName.equals("InactiveAccountException")
                    || exceptionName.equals("DuplicateTransactionException")) {

                metricsCollector.recordRejected(duration);

            } else {
                metricsCollector.recordTechnicalFailure(duration);
            }

            throw ex;

        } finally {
            metricsCollector.recordResourceUsage();
        }

    }, transactionExecutor);
}


    public CompletableFuture<Account> processDeposit(
            String accountNumber, BigDecimal amount) {

        return submit(() -> transactionService.deposit(accountNumber, amount));
    }

    public CompletableFuture<Account> processWithdrawal(
            String accountNumber, BigDecimal amount) {

        return submit(() -> transactionService.withdraw(accountNumber, amount));
    }

    public CompletableFuture<Map<String, Account>> processTransfer(
            String fromAccountNumber,
            String toAccountNumber,
            BigDecimal amount) {

        return submit(() -> transactionService.transfer(
                fromAccountNumber, toAccountNumber, amount
        ));
    }
}
