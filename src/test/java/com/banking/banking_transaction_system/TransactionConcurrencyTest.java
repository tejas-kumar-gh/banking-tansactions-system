package com.banking.banking_transaction_system;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.banking.banking_transaction_system.service.TransactionService;

@SpringBootTest
class TransactionConcurrencyTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldPreventConcurrentWithdrawalsFromOverdrawingAccount()
            throws Exception {

        String accountNumber = "ACC1001";

        jdbcTemplate.update(
                "UPDATE account SET balance = 10000.00 WHERE account_number = ?",
                accountNumber
        );

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Boolean> withdrawal1 = () -> {
            ready.countDown();
            start.await();

            try {
                transactionService.withdraw(
                        accountNumber, new BigDecimal("7000.00")
                );
                return true;
            } catch (RuntimeException ex) {
                return false;
            }
        };

        Callable<Boolean> withdrawal2 = () -> {
            ready.countDown();
            start.await();

            try {
                transactionService.withdraw(
                        accountNumber, new BigDecimal("5000.00")
                );
                return true;
            } catch (RuntimeException ex) {
                return false;
            }
        };

        try {
            Future<Boolean> result1 = executor.submit(withdrawal1);
            Future<Boolean> result2 = executor.submit(withdrawal2);

            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();

            boolean firstSucceeded = result1.get(15, TimeUnit.SECONDS);
            boolean secondSucceeded = result2.get(15, TimeUnit.SECONDS);

            BigDecimal finalBalance = jdbcTemplate.queryForObject(
                    "SELECT balance FROM account WHERE account_number = ?",
                    BigDecimal.class,
                    accountNumber
            );

            assertNotEquals(firstSucceeded, secondSucceeded);
            assertTrue(finalBalance.compareTo(BigDecimal.ZERO) >= 0);

            assertEquals(
                    1,
                    (firstSucceeded ? 1 : 0) + (secondSucceeded ? 1 : 0)
            );

            System.out.println("Withdrawal 1 success: " + firstSucceeded);
            System.out.println("Withdrawal 2 success: " + secondSucceeded);
            System.out.println("Final balance: " + finalBalance);

        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }
}
