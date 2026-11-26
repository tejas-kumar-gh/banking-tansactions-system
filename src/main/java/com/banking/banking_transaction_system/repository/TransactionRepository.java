package com.banking.banking_transaction_system.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class TransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    public TransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(
            String transactionId,
            Long sourceAccountId,
            Long destinationAccountId,
            BigDecimal amount,
            String transactionType,
            String transactionStatus) {

        String sql = """
            INSERT INTO banking_transaction
            (transaction_id, source_account_id, destination_account_id,
             amount, transaction_type, transaction_status)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        jdbcTemplate.update(
            sql,
            transactionId,
            sourceAccountId,
            destinationAccountId,
            amount,
            transactionType,
            transactionStatus
        );
    }
}