package com.banking.banking_transaction_system.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.banking.banking_transaction_system.model.Transaction;

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

    save(
        transactionId,
        null,
        sourceAccountId,
        destinationAccountId,
        amount,
        transactionType,
        transactionStatus
    );
}

public void save(
        String transactionId,
        String idempotencyKey,
        Long sourceAccountId,
        Long destinationAccountId,
        BigDecimal amount,
        String transactionType,
        String transactionStatus) {

    String sql = """
        INSERT INTO banking_transaction
        (transaction_id, idempotency_key, source_account_id,
         destination_account_id, amount, transaction_type,
         transaction_status)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    jdbcTemplate.update(
        sql,
        transactionId,
        idempotencyKey,
        sourceAccountId,
        destinationAccountId,
        amount,
        transactionType,
        transactionStatus
    );
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

    public Optional<Transaction> findByIdempotencyKey(String idempotencyKey) {

    String sql = """
        SELECT transaction_record_id, transaction_id, idempotency_key,
               source_account_id, destination_account_id,
               amount, transaction_type, transaction_status, created_at
        FROM banking_transaction
        WHERE idempotency_key = ?
        """;

    List<Transaction> transactions = jdbcTemplate.query(sql, (rs, rowNum) -> {
        Transaction transaction = new Transaction();

        transaction.setTransactionRecordId(
            rs.getLong("transaction_record_id")
        );
        transaction.setTransactionId(rs.getString("transaction_id"));
        transaction.setIdempotencyKey(rs.getString("idempotency_key"));
        transaction.setSourceAccountId(
            rs.getObject("source_account_id", Long.class)
        );
        transaction.setDestinationAccountId(
            rs.getObject("destination_account_id", Long.class)
        );
        transaction.setAmount(rs.getBigDecimal("amount"));
        transaction.setTransactionType(rs.getString("transaction_type"));
        transaction.setTransactionStatus(rs.getString("transaction_status"));
        transaction.setCreatedAt(
            rs.getTimestamp("created_at").toLocalDateTime()
        );

        return transaction;
    }, idempotencyKey);

    return transactions.stream().findFirst();
}

public void updateStatus(String transactionId, String status) {

    String sql = """
        UPDATE banking_transaction
        SET transaction_status = ?
        WHERE transaction_id = ?
        """;

    jdbcTemplate.update(sql, status, transactionId);
}
}