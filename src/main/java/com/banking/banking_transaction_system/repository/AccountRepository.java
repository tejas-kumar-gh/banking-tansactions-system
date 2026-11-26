
    package com.banking.banking_transaction_system.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.banking.banking_transaction_system.model.Account;

@Repository
public class AccountRepository {

    private final JdbcTemplate jdbcTemplate;

    public AccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Account save(Account account) {

        String sql = """
                INSERT INTO account
                (account_number, customer_id, balance, account_status)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {

            PreparedStatement ps = connection.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS
            );

            ps.setString(1, account.getAccountNumber());
            ps.setLong(2, account.getCustomerId());
            ps.setBigDecimal(3, account.getBalance());
            ps.setString(4, account.getAccountStatus());

            return ps;

        }, keyHolder);

        Number generatedId = keyHolder.getKey();

        account.setAccountId(generatedId.longValue());

        return account;
    }

    public Optional<Account> findByAccountNumber(String accountNumber) {

        String sql = """
                SELECT account_id, account_number, customer_id,
                       balance, account_status
                FROM account
                WHERE account_number = ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Account(
                        rs.getLong("account_id"),
                        rs.getString("account_number"),
                        rs.getLong("customer_id"),
                        rs.getBigDecimal("balance"),
                        rs.getString("account_status")
                ), accountNumber
        ).stream().findFirst();
    }


public Optional<Account> findByAccountNumberForUpdate(String accountNumber) {

    String sql = """
            SELECT account_id, account_number, customer_id,
                   balance, account_status
            FROM account
            WHERE account_number = ?
            FOR UPDATE
            """;

    return jdbcTemplate.query(sql, (rs, rowNum) ->
            new Account(
                    rs.getLong("account_id"),
                    rs.getString("account_number"),
                    rs.getLong("customer_id"),
                    rs.getBigDecimal("balance"),
                    rs.getString("account_status")
            ), accountNumber
    ).stream().findFirst();
}

public void updateBalance(Long accountId, java.math.BigDecimal newBalance) {

    String sql = """
            UPDATE account
            SET balance = ?
            WHERE account_id = ?
            """;

    jdbcTemplate.update(sql, newBalance, accountId);
}

}
