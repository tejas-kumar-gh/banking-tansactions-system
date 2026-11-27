package com.banking.banking_transaction_system.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.banking.banking_transaction_system.repository.TransactionRepository;
import com.banking.banking_transaction_system.repository.FailedTransactionRepository;
import com.banking.banking_transaction_system.repository.TransactionAuditRepository;

@Service
public class FailureRecordingService {

    private final TransactionRepository transactionRepository;
    private final FailedTransactionRepository failedTransactionRepository;
    private final TransactionAuditRepository transactionAuditRepository;

    public FailureRecordingService(
            TransactionRepository transactionRepository,
            FailedTransactionRepository failedTransactionRepository,
            TransactionAuditRepository transactionAuditRepository) {

        this.transactionRepository = transactionRepository;
        this.failedTransactionRepository = failedTransactionRepository;
        this.transactionAuditRepository = transactionAuditRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(
            String transactionId,
            Long sourceAccountId,
            Long destinationAccountId,
            BigDecimal amount,
            String transactionType,
            String failureReason,
            String errorCategory) {

        transactionRepository.save(
                transactionId,
                sourceAccountId,
                destinationAccountId,
                amount,
                transactionType,
                "FAILED"
        );

        failedTransactionRepository.save(
                transactionId,
                failureReason,
                errorCategory
        );

        transactionAuditRepository.save(
                transactionId,
                "FAILED",
                failureReason
        );
    }
}