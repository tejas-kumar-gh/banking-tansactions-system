
package com.banking.banking_transaction_system.service;

import java.math.BigDecimal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banking.banking_transaction_system.model.Account;
import com.banking.banking_transaction_system.repository.AccountRepository;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final JdbcTemplate jdbcTemplate;

    public AccountService(AccountRepository accountRepository,
                          JdbcTemplate jdbcTemplate) {
        this.accountRepository = accountRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Account createAccount(Long customerId, BigDecimal initialBalance) {

        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }

        String customerSql = """
                SELECT COUNT(*)
                FROM customer
                WHERE customer_id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(
                customerSql, Integer.class, customerId
        );

        if (count == null || count == 0) {
            throw new IllegalArgumentException("Customer not found");
        }

        String accountNumber = "ACC" + System.currentTimeMillis();

        Account account = new Account(
                null,
                accountNumber,
                customerId,
                initialBalance,
                "ACTIVE"
        );

        return accountRepository.save(account);
    }
}
