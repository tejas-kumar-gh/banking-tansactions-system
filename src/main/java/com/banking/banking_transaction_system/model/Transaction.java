package com.banking.banking_transaction_system.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    private Long transactionRecordId;
    private String transactionId;
    private Long sourceAccountId;
    private Long destinationAccountId;
    private BigDecimal amount;
    private String transactionType;
    private String transactionStatus;
    private LocalDateTime createdAt;
    private String idempotencyKey;
    public Transaction() {
    }


    public Transaction(
            Long transactionRecordId,
            String transactionId,
            Long sourceAccountId,
            Long destinationAccountId,
            BigDecimal amount,
            String transactionType,
            String transactionStatus,
            LocalDateTime createdAt) {

        this.transactionRecordId = transactionRecordId;
        this.transactionId = transactionId;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.transactionStatus = transactionStatus;
        this.createdAt = createdAt;
    }

    public Long getTransactionRecordId() {
        return transactionRecordId;
    }

    public void setTransactionRecordId(Long transactionRecordId) {
        this.transactionRecordId = transactionRecordId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Long getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(Long sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public Long getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(Long destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public String getIdempotencyKey() {
    return idempotencyKey;
}

public void setIdempotencyKey(String idempotencyKey) {
    this.idempotencyKey = idempotencyKey;
}
}