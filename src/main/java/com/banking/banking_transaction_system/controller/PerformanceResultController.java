
package com.banking.banking_transaction_system.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banking.banking_transaction_system.model.PerformanceTestResult;
import com.banking.banking_transaction_system.repository.PerformanceTestRepository;

@RestController
@RequestMapping("/api/performance")
public class PerformanceResultController {

    private final PerformanceTestRepository repository;

    public PerformanceResultController(
            PerformanceTestRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/results")
    public List<PerformanceTestResult> getAllResults() {
        return repository.findAllResults();
    }

    @GetMapping("/compare")
    public List<PerformanceTestResult> compareWorkerPerformance() {
        return repository.compareWorkerPerformance();
    }
}
