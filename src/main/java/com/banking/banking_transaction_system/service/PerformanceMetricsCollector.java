
package com.banking.banking_transaction_system.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.banking.banking_transaction_system.model.PerformanceMetrics;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

@Service
public class PerformanceMetricsCollector {

    private final ExecutorService transactionExecutor;
    private final HikariDataSource dataSource;

    private final ScheduledExecutorService monitoringExecutor =
            Executors.newSingleThreadScheduledExecutor(task -> {
                Thread thread = new Thread(task, "bank-metrics-monitor");
                thread.setDaemon(true);
                return thread;
            });

    private ScheduledFuture<?> monitoringTask;

    private volatile PerformanceMetrics currentMetrics;

    public PerformanceMetricsCollector(
            @Qualifier("transactionExecutor") ExecutorService transactionExecutor,
            DataSource dataSource) {

        this.transactionExecutor = transactionExecutor;

        try {
            this.dataSource = dataSource.unwrap(HikariDataSource.class);
        } catch (java.sql.SQLException e) {
            throw new IllegalStateException(
                    "Unable to access HikariCP DataSource", e);
        }
    }


    public synchronized void startRun() {

        if (monitoringTask != null) {
            monitoringTask.cancel(false);
        }

        currentMetrics = new PerformanceMetrics();
        currentMetrics.start();

        monitoringTask = monitoringExecutor.scheduleAtFixedRate(
                this::recordResourceUsage,
                0,
                10,
                TimeUnit.MILLISECONDS
        );
    }

    private PerformanceMetrics getMetrics() {

        if (currentMetrics == null) {
            synchronized (this) {
                if (currentMetrics == null) {
                    startRun();
                }
            }
        }

        return currentMetrics;
    }

    public void recordSubmitted() {
        getMetrics().recordSubmitted();
    }

    public void recordSuccess(long duration) {
        getMetrics().recordSuccess(duration);
    }

    public void recordBusinessFailure(long duration) {
        getMetrics().recordBusinessFailure(duration);
    }

    public void recordRejected(long duration) {
        getMetrics().recordRejected(duration);
    }

    public void recordTechnicalFailure(long duration) {
        getMetrics().recordTechnicalFailure(duration);
    }

    public void recordConnectionWaitTime(long waitTime) {
    getMetrics().recordConnectionWaitTime(waitTime);
}

    public void recordResourceUsage() {

        PerformanceMetrics metrics = getMetrics();

        if (transactionExecutor instanceof ThreadPoolExecutor pool) {
            metrics.recordThreadUsage(
                    pool.getActiveCount(),
                    pool.getQueue().size()
            );
        }

        HikariPoolMXBean poolBean = dataSource.getHikariPoolMXBean();

        if (poolBean != null) {
            metrics.recordDatabaseUsage(
                    poolBean.getActiveConnections(),
                    poolBean.getIdleConnections(),
                    poolBean.getThreadsAwaitingConnection()
            );
        }
    }

    public synchronized PerformanceMetrics finishRun() {

        if (monitoringTask != null) {
            monitoringTask.cancel(false);
            monitoringTask = null;
        }

        recordResourceUsage();

        currentMetrics.finish();

        return currentMetrics;
    }

    public PerformanceMetrics getCurrentMetrics() {
        return getMetrics();
    }
}
