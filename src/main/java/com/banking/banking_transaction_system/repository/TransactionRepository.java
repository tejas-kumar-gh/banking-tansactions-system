package com.banking.banking_transaction_system.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.banking.banking_transaction_system.model.Transaction;

import java.math.BigDecimal;
import java.util.List;

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
                transactionStatus);
    }

    public List<Transaction> findByAccountId(Long accountId) {

        String sql = """
                    SELECT
                        transaction_record_id,
                        transaction_id,
                        source_account_id,
                        destination_account_id,
                        amount,
                        transaction_type,
                        transaction_status,
                        created_at
                    FROM banking_transaction
                    WHERE source_account_id = ?
                       OR destination_account_id = ?
                    ORDER BY transaction_record_id DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Transaction(
                rs.getLong("transaction_record_id"),
                rs.getString("transaction_id"),
                rs.getObject("source_account_id", Long.class),
                rs.getObject("destination_account_id", Long.class),
                rs.getBigDecimal("amount"),
                rs.getString("transaction_type"),
                rs.getString("transaction_status"),
                rs.getTimestamp("created_at").toLocalDateTime()), accountId, accountId);
    }
}