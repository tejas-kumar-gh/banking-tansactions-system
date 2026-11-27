package com.banking.banking_transaction_system.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FailedTransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    public FailedTransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(
            String transactionId,
            String failureReason,
            String errorCategory) {

        String sql = """
            INSERT INTO failed_transaction
            (transaction_id, failure_reason, error_category)
            VALUES (?, ?, ?)
        """;

        jdbcTemplate.update(
                sql,
                transactionId,
                failureReason,
                errorCategory
        );
    }
}