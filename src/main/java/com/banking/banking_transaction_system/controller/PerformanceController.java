
package com.banking.banking_transaction_system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.banking.banking_transaction_system.model.PerformanceMetrics;
import com.banking.banking_transaction_system.model.PerformanceTestRequest;
import com.banking.banking_transaction_system.service.PerformanceTestRunner;

@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    private final PerformanceTestRunner performanceTestRunner;

    public PerformanceController(PerformanceTestRunner performanceTestRunner) {
        this.performanceTestRunner = performanceTestRunner;
    }

    @PostMapping("/run")
    public ResponseEntity<PerformanceMetrics> runPerformanceTest(
            @RequestBody PerformanceTestRequest request) {

        if (request.getTransactionCount() <= 0) {
            throw new IllegalArgumentException(
                    "Transaction count must be greater than zero"
            );
        }

        PerformanceMetrics result =
                performanceTestRunner.runDepositBenchmark(
                        request.getTransactionCount()
                );

        return ResponseEntity.ok(result);
    }
}
