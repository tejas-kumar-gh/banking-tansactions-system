
package com.banking.banking_transaction_system.model;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PerformanceMetrics {

    
    private final AtomicInteger peakIdleConnections = new AtomicInteger();

    private final AtomicInteger peakPendingThreads = new AtomicInteger();
    private final AtomicLong maximumConnectionWaitTime = new AtomicLong();
    private final AtomicLong totalConnectionWaitTime = new AtomicLong();
    private final AtomicInteger totalTransactions = new AtomicInteger();
    private final AtomicInteger successCount = new AtomicInteger();
    private final AtomicInteger failedCount = new AtomicInteger();
    private final AtomicInteger rejectedCount = new AtomicInteger();
    private final AtomicInteger technicalErrorCount = new AtomicInteger();

    private final AtomicLong totalTransactionTime = new AtomicLong();
    private final AtomicLong minimumTransactionTime = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maximumTransactionTime = new AtomicLong();

    private final AtomicInteger peakActiveThreads = new AtomicInteger();
    private final AtomicInteger peakQueueSize = new AtomicInteger();
    private final AtomicInteger peakDbConnections = new AtomicInteger();

    private long startTime;
    private long endTime;

    public void start() {
        startTime = System.nanoTime();
    }

    public void recordSuccess(long duration) {
        successCount.incrementAndGet();
        recordDuration(duration);
    }

    public void recordRejected(long duration) {
        rejectedCount.incrementAndGet();
        recordDuration(duration);
    }

    public void recordTechnicalFailure(long duration) {
        failedCount.incrementAndGet();
        technicalErrorCount.incrementAndGet();
        recordDuration(duration);
    }

    public void recordBusinessFailure(long duration) {
        failedCount.incrementAndGet();
        recordDuration(duration);
    }

    public void recordDuration(long duration) {
        totalTransactionTime.addAndGet(duration);
        minimumTransactionTime.accumulateAndGet(duration, Math::min);
        maximumTransactionTime.accumulateAndGet(duration, Math::max);
    }

    public void recordSubmitted() {
        totalTransactions.incrementAndGet();
    }

    public void recordThreadUsage(int active, int queueSize) {
        peakActiveThreads.accumulateAndGet(active, Math::max);
        peakQueueSize.accumulateAndGet(queueSize, Math::max);
    }

    public void recordDbConnections(int activeConnections) {
        peakDbConnections.accumulateAndGet(activeConnections, Math::max);
    }

    public void finish() {
        endTime = System.nanoTime();
    }

    public int getTotalTransactions() {
        return totalTransactions.get();
    }

    public int getSuccessCount() {
        return successCount.get();
    }

    public int getFailedCount() {
        return failedCount.get();
    }

    public int getRejectedCount() {
        return rejectedCount.get();
    }

    public int getTechnicalErrorCount() {
        return technicalErrorCount.get();
    }

    public long getDurationMs() {
        return (endTime - startTime) / 1_000_000;
    }

    public BigDecimal getThroughput() {
        double seconds = (endTime - startTime) / 1_000_000_000.0;
        return seconds <= 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(totalTransactions.get() / seconds);
    }

    public BigDecimal getAverageDurationMs() {
        int completed = successCount.get() + failedCount.get();
        return completed == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(totalTransactionTime.get() / (double) completed / 1_000_000);
    }

    public long getMinimumDurationMs() {
        return minimumTransactionTime.get() == Long.MAX_VALUE
                ? 0
                : minimumTransactionTime.get() / 1_000_000;
    }

    public long getMaximumDurationMs() {
        return maximumTransactionTime.get() / 1_000_000;
    }

    public int getPeakActiveThreads() {
        return peakActiveThreads.get();
    }

    public int getPeakQueueSize() {
        return peakQueueSize.get();
    }

    public int getPeakDbConnections() {
        return peakDbConnections.get();
    }
    public void recordDatabaseUsage(
        int active,
        int idle,
        int pending) {

    peakDbConnections.accumulateAndGet(active, Math::max);
    peakIdleConnections.accumulateAndGet(idle, Math::max);
    peakPendingThreads.accumulateAndGet(pending, Math::max);
}


public void recordConnectionWaitTime(long waitTime) {
    totalConnectionWaitTime.addAndGet(waitTime);
    maximumConnectionWaitTime.accumulateAndGet(waitTime, Math::max);
}

public double getMaximumConnectionWaitTime() {
    return maximumConnectionWaitTime.get() / 1_000_000.0;
}
public int getPeakIdleConnections() {
    return peakIdleConnections.get();
}

public int getPeakPendingThreads() {
    return peakPendingThreads.get();
}

public double getTotalConnectionWaitTime() {
    return totalConnectionWaitTime.get() / 1_000_000.0;
}
}
