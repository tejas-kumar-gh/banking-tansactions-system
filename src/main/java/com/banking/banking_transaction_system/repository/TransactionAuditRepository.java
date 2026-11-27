package com.banking.banking_transaction_system.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionAuditRepository {

    private final JdbcTemplate jdbcTemplate;

    public TransactionAuditRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(
            String transactionId,
            String eventType,
            String description) {

        String sql = """
            INSERT INTO transaction_audit
            (transaction_id, event_type, description)
            VALUES (?, ?, ?)
        """;

        jdbcTemplate.update(
                sql,
                transactionId,
                eventType,
                description
        );
    }
}