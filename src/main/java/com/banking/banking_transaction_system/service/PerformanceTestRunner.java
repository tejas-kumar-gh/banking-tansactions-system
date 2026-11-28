
package com.banking.banking_transaction_system.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.banking.banking_transaction_system.model.PerformanceMetrics;
import com.banking.banking_transaction_system.repository.PerformanceTestRepository;

@Service
public class PerformanceTestRunner {

    private final TransactionProcessingService processingService;
    private final PerformanceMetricsCollector metricsCollector;
    private final PerformanceTestRepository performanceTestRepository;

    @Value("${banking.executor.threads:5}")
    private int workerCount;

    @Value("${banking.performance.batch-size:500}")
    private int batchSize;

    private final List<String> testAccounts = List.of(
            "ACC1001",
            "ACC1002",
            "ACC1003",
            "ACC1004",
            "ACC1005"
    );

    public PerformanceTestRunner(
            TransactionProcessingService processingService,
            PerformanceMetricsCollector metricsCollector,
            PerformanceTestRepository performanceTestRepository) {

        this.processingService = processingService;
        this.metricsCollector = metricsCollector;
        this.performanceTestRepository = performanceTestRepository;
    }

    public synchronized PerformanceMetrics runDepositBenchmark(
            int transactionCount) {

        if (transactionCount <= 0) {
            throw new IllegalArgumentException(
                    "Transaction count must be greater than zero"
            );
        }

        LocalDateTime startedAt = LocalDateTime.now();

        metricsCollector.startRun();

        BigDecimal amount = new BigDecimal("1.00");

        List<CompletableFuture<Void>> tasks =
                new ArrayList<>(batchSize);

        for (int i = 0; i < transactionCount; i++) {

            String accountNumber =
                    testAccounts.get(i % testAccounts.size());

            CompletableFuture<Void> task =
                    processingService
                            .processDeposit(accountNumber, amount)
                            .handle((result, error) -> null);

            tasks.add(task);

            if (tasks.size() == batchSize || i == transactionCount - 1) {

                CompletableFuture.allOf(
                        tasks.toArray(new CompletableFuture<?>[0])
                ).join();

                tasks.clear();
            }
        }

        PerformanceMetrics metrics = metricsCollector.finishRun();

        LocalDateTime completedAt = LocalDateTime.now();

        Long runId = performanceTestRepository.savePerformanceResult(
                "Deposit benchmark - " + transactionCount + " transactions",
                workerCount,
                transactionCount,
                startedAt,
                completedAt,
                metrics
        );

        System.out.println("Performance test saved. Run ID: " + runId);

        return metrics;
    }
}
