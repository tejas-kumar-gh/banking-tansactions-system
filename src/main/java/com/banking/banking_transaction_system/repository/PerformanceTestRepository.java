
package com.banking.banking_transaction_system.repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.banking.banking_transaction_system.model.PerformanceMetrics;
import com.banking.banking_transaction_system.model.PerformanceTestResult;

@Repository
public class PerformanceTestRepository {

        private final JdbcTemplate jdbcTemplate;

        @Value("${banking.executor.queue-capacity:1000}")
        private int queueCapacity;

        @Value("${spring.datasource.hikari.maximum-pool-size:25}")
        private int dbMaxPoolSize;

        public PerformanceTestRepository(JdbcTemplate jdbcTemplate) {
                this.jdbcTemplate = jdbcTemplate;
        }

        @Transactional
        public Long savePerformanceResult(
                        String runName,
                        int workerCount,
                        int totalTransactions,
                        LocalDateTime startedAt,
                        LocalDateTime completedAt,
                        PerformanceMetrics metrics) {

                String runSql = """
                                INSERT INTO performance_test_run
                                (run_name, worker_count, total_transactions, test_status,
                                 started_at, completed_at, duration_ms)
                                VALUES (?, ?, ?, ?, ?, ?, ?)
                                """;

                KeyHolder keyHolder = new GeneratedKeyHolder();

                jdbcTemplate.update(connection -> {
                        PreparedStatement ps = connection.prepareStatement(
                                        runSql, Statement.RETURN_GENERATED_KEYS);

                        ps.setString(1, runName);
                        ps.setInt(2, workerCount);
                        ps.setInt(3, totalTransactions);
                        ps.setString(4, "COMPLETED");
                        ps.setTimestamp(5, Timestamp.valueOf(startedAt));
                        ps.setTimestamp(6, Timestamp.valueOf(completedAt));
                        ps.setLong(7, metrics.getDurationMs());

                        return ps;
                }, keyHolder);

                Long runId = keyHolder.getKey().longValue();

                BigDecimal successRate = calculateRate(
                                metrics.getSuccessCount(), totalTransactions);

                BigDecimal failureRate = calculateRate(
                                metrics.getFailedCount() + metrics.getRejectedCount(),
                                totalTransactions);

                String metricsSql = """
                                INSERT INTO performance_test_metrics
                                (run_id, success_count, failed_count, rejected_count,
                                 technical_error_count, average_duration_ms,
                                 minimum_duration_ms, maximum_duration_ms,
                                 throughput_tps, success_rate, failure_rate,
                                 peak_active_threads, queue_capacity,
                                 db_max_pool_size, peak_db_active_connections,
                                 peak_db_idle_connections, peak_db_pending_threads)
                                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                                """;

                jdbcTemplate.update(
                                metricsSql,
                                runId,
                                metrics.getSuccessCount(),
                                metrics.getFailedCount(),
                                metrics.getRejectedCount(),
                                metrics.getTechnicalErrorCount(),
                                metrics.getAverageDurationMs(),
                                metrics.getMinimumDurationMs(),
                                metrics.getMaximumDurationMs(),
                                metrics.getThroughput(),
                                successRate,
                                failureRate,
                                metrics.getPeakActiveThreads(),
                                queueCapacity,
                                dbMaxPoolSize,
                                metrics.getPeakDbConnections(),
                                metrics.getPeakIdleConnections(),
                                metrics.getPeakPendingThreads());

                return runId;
        }

        private BigDecimal calculateRate(int count, int total) {
                if (total == 0) {
                        return BigDecimal.ZERO;
                }

                return BigDecimal.valueOf(count)
                                .multiply(BigDecimal.valueOf(100))
                                .divide(BigDecimal.valueOf(total), 3, RoundingMode.HALF_UP);
        }

        public List<PerformanceTestResult> findAllResults() {

                String sql = """
                                SELECT r.run_id, r.run_name, r.worker_count,
                                       r.total_transactions, r.test_status,
                                       r.started_at, r.completed_at, r.duration_ms,
                                       m.success_count, m.failed_count, m.rejected_count,
                                       m.technical_error_count, m.average_duration_ms,
                                       m.minimum_duration_ms, m.maximum_duration_ms,
                                       m.throughput_tps, m.success_rate, m.failure_rate,
                                       m.peak_active_threads, m.queue_capacity,
                                       m.db_max_pool_size, m.peak_db_active_connections,
                                       m.peak_db_idle_connections, m.peak_db_pending_threads
                                FROM performance_test_run r
                                JOIN performance_test_metrics m ON r.run_id = m.run_id
                                ORDER BY r.run_id DESC
                                """;

                return jdbcTemplate.query(sql, (rs, rowNum) -> {

                        PerformanceTestResult result = new PerformanceTestResult();

                        result.setRunId(rs.getLong("run_id"));
                        result.setRunName(rs.getString("run_name"));
                        result.setWorkerCount(rs.getInt("worker_count"));
                        result.setTotalTransactions(rs.getInt("total_transactions"));
                        result.setTestStatus(rs.getString("test_status"));

                        result.setStartedAt(rs.getTimestamp("started_at").toLocalDateTime());
                        result.setCompletedAt(rs.getTimestamp("completed_at").toLocalDateTime());
                        result.setDurationMs(rs.getLong("duration_ms"));

                        result.setSuccessCount(rs.getInt("success_count"));
                        result.setFailedCount(rs.getInt("failed_count"));
                        result.setRejectedCount(rs.getInt("rejected_count"));
                        result.setTechnicalErrorCount(rs.getInt("technical_error_count"));

                        result.setAverageDurationMs(rs.getBigDecimal("average_duration_ms"));
                        result.setMinimumDurationMs(rs.getLong("minimum_duration_ms"));
                        result.setMaximumDurationMs(rs.getLong("maximum_duration_ms"));

                        result.setThroughputTps(rs.getBigDecimal("throughput_tps"));
                        result.setSuccessRate(rs.getBigDecimal("success_rate"));
                        result.setFailureRate(rs.getBigDecimal("failure_rate"));

                        result.setPeakActiveThreads(rs.getInt("peak_active_threads"));
                        result.setQueueCapacity(rs.getInt("queue_capacity"));
                        result.setDbMaxPoolSize(rs.getInt("db_max_pool_size"));
                        result.setPeakDbActiveConnections(rs.getInt("peak_db_active_connections"));
                        result.setPeakDbIdleConnections(rs.getInt("peak_db_idle_connections"));
                        result.setPeakDbPendingThreads(rs.getInt("peak_db_pending_threads"));

                        return result;
                });
        }


public List<PerformanceTestResult> compareWorkerPerformance() {

    String sql = """
            SELECT r.run_id, r.run_name, r.worker_count,
                   r.total_transactions, r.test_status,
                   r.started_at, r.completed_at, r.duration_ms,
                   m.success_count, m.failed_count, m.rejected_count,
                   m.technical_error_count, m.average_duration_ms,
                   m.minimum_duration_ms, m.maximum_duration_ms,
                   m.throughput_tps, m.success_rate, m.failure_rate,
                   m.peak_active_threads, m.queue_capacity,
                   m.db_max_pool_size, m.peak_db_active_connections,
                   m.peak_db_idle_connections, m.peak_db_pending_threads
            FROM performance_test_run r
            JOIN performance_test_metrics m ON r.run_id = m.run_id
            WHERE r.run_id IN (
                SELECT MAX(run_id)
                FROM performance_test_run
                WHERE test_status = 'COMPLETED'
                GROUP BY worker_count, total_transactions
            )
            ORDER BY r.worker_count
            """;

    return jdbcTemplate.query(sql, (rs, rowNum) -> {

        PerformanceTestResult result = new PerformanceTestResult();

        result.setRunId(rs.getLong("run_id"));
        result.setRunName(rs.getString("run_name"));
        result.setWorkerCount(rs.getInt("worker_count"));
        result.setTotalTransactions(rs.getInt("total_transactions"));
        result.setTestStatus(rs.getString("test_status"));
        result.setStartedAt(rs.getTimestamp("started_at").toLocalDateTime());
        result.setCompletedAt(rs.getTimestamp("completed_at").toLocalDateTime());
        result.setDurationMs(rs.getLong("duration_ms"));

        result.setSuccessCount(rs.getInt("success_count"));
        result.setFailedCount(rs.getInt("failed_count"));
        result.setRejectedCount(rs.getInt("rejected_count"));
        result.setTechnicalErrorCount(rs.getInt("technical_error_count"));

        result.setAverageDurationMs(rs.getBigDecimal("average_duration_ms"));
        result.setMinimumDurationMs(rs.getLong("minimum_duration_ms"));
        result.setMaximumDurationMs(rs.getLong("maximum_duration_ms"));

        result.setThroughputTps(rs.getBigDecimal("throughput_tps"));
        result.setSuccessRate(rs.getBigDecimal("success_rate"));
        result.setFailureRate(rs.getBigDecimal("failure_rate"));

        result.setPeakActiveThreads(rs.getInt("peak_active_threads"));
        result.setQueueCapacity(rs.getInt("queue_capacity"));
        result.setDbMaxPoolSize(rs.getInt("db_max_pool_size"));
        result.setPeakDbActiveConnections(rs.getInt("peak_db_active_connections"));
        result.setPeakDbIdleConnections(rs.getInt("peak_db_idle_connections"));
        result.setPeakDbPendingThreads(rs.getInt("peak_db_pending_threads"));

        return result;
    });
}


}
