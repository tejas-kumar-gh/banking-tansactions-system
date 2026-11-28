
package com.banking.banking_transaction_system.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PerformanceTestResult {

    private Long runId;
    private String runName;
    private Integer workerCount;
    private Integer totalTransactions;
    private String testStatus;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long durationMs;

    private Integer successCount;
    private Integer failedCount;
    private Integer rejectedCount;
    private Integer technicalErrorCount;

    private BigDecimal averageDurationMs;
    private Long minimumDurationMs;
    private Long maximumDurationMs;

    private BigDecimal throughputTps;
    private BigDecimal successRate;
    private BigDecimal failureRate;

    private Integer peakActiveThreads;
    private Integer queueCapacity;
    private Integer dbMaxPoolSize;
    private Integer peakDbActiveConnections;
    private Integer peakDbIdleConnections;
    private Integer peakDbPendingThreads;

    public Long getRunId() { return runId; }
    public void setRunId(Long runId) { this.runId = runId; }

    public String getRunName() { return runName; }
    public void setRunName(String runName) { this.runName = runName; }

    public Integer getWorkerCount() { return workerCount; }
    public void setWorkerCount(Integer workerCount) { this.workerCount = workerCount; }

    public Integer getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(Integer totalTransactions) { this.totalTransactions = totalTransactions; }

    public String getTestStatus() { return testStatus; }
    public void setTestStatus(String testStatus) { this.testStatus = testStatus; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public Integer getSuccessCount() { return successCount; }
    public void setSuccessCount(Integer successCount) { this.successCount = successCount; }

    public Integer getFailedCount() { return failedCount; }
    public void setFailedCount(Integer failedCount) { this.failedCount = failedCount; }

    public Integer getRejectedCount() { return rejectedCount; }
    public void setRejectedCount(Integer rejectedCount) { this.rejectedCount = rejectedCount; }

    public Integer getTechnicalErrorCount() { return technicalErrorCount; }
    public void setTechnicalErrorCount(Integer technicalErrorCount) { this.technicalErrorCount = technicalErrorCount; }

    public BigDecimal getAverageDurationMs() { return averageDurationMs; }
    public void setAverageDurationMs(BigDecimal averageDurationMs) { this.averageDurationMs = averageDurationMs; }

    public Long getMinimumDurationMs() { return minimumDurationMs; }
    public void setMinimumDurationMs(Long minimumDurationMs) { this.minimumDurationMs = minimumDurationMs; }

    public Long getMaximumDurationMs() { return maximumDurationMs; }
    public void setMaximumDurationMs(Long maximumDurationMs) { this.maximumDurationMs = maximumDurationMs; }

    public BigDecimal getThroughputTps() { return throughputTps; }
    public void setThroughputTps(BigDecimal throughputTps) { this.throughputTps = throughputTps; }

    public BigDecimal getSuccessRate() { return successRate; }
    public void setSuccessRate(BigDecimal successRate) { this.successRate = successRate; }

    public BigDecimal getFailureRate() { return failureRate; }
    public void setFailureRate(BigDecimal failureRate) { this.failureRate = failureRate; }

    public Integer getPeakActiveThreads() { return peakActiveThreads; }
    public void setPeakActiveThreads(Integer peakActiveThreads) { this.peakActiveThreads = peakActiveThreads; }

    public Integer getQueueCapacity() { return queueCapacity; }
    public void setQueueCapacity(Integer queueCapacity) { this.queueCapacity = queueCapacity; }

    public Integer getDbMaxPoolSize() { return dbMaxPoolSize; }
    public void setDbMaxPoolSize(Integer dbMaxPoolSize) { this.dbMaxPoolSize = dbMaxPoolSize; }

    public Integer getPeakDbActiveConnections() { return peakDbActiveConnections; }
    public void setPeakDbActiveConnections(Integer peakDbActiveConnections) { this.peakDbActiveConnections = peakDbActiveConnections; }

    public Integer getPeakDbIdleConnections() { return peakDbIdleConnections; }
    public void setPeakDbIdleConnections(Integer peakDbIdleConnections) { this.peakDbIdleConnections = peakDbIdleConnections; }

    public Integer getPeakDbPendingThreads() { return peakDbPendingThreads; }
    public void setPeakDbPendingThreads(Integer peakDbPendingThreads) { this.peakDbPendingThreads = peakDbPendingThreads; }
}
