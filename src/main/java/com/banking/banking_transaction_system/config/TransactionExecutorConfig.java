
package com.banking.banking_transaction_system.config;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransactionExecutorConfig {

    @Value("${banking.executor.threads:5}")
    private int workerCount;

    @Value("${banking.executor.queue-capacity:1000}")
    private int queueCapacity;

    @Bean(name = "transactionExecutor", destroyMethod = "shutdown")
    public ExecutorService transactionExecutor() {

        AtomicInteger threadNumber = new AtomicInteger(1);

        ThreadFactory threadFactory = task ->
                new Thread(
                        task,
                        "bank-transaction-" + threadNumber.getAndIncrement()
                );

        return new ThreadPoolExecutor(
                workerCount,
                workerCount,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
