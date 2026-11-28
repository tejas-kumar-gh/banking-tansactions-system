
package com.banking.banking_transaction_system.config;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.stereotype.Component;

import com.banking.banking_transaction_system.service.PerformanceMetricsCollector;

@Component
public class DataSourceMonitoringPostProcessor implements BeanPostProcessor {

    private final ObjectProvider<PerformanceMetricsCollector> metricsCollectorProvider;

    public DataSourceMonitoringPostProcessor(
            ObjectProvider<PerformanceMetricsCollector> metricsCollectorProvider) {
        this.metricsCollectorProvider = metricsCollectorProvider;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {

        if ("dataSource".equals(beanName) && bean instanceof DataSource dataSource) {

            return new DelegatingDataSource(dataSource) {

                @Override
                public Connection getConnection() throws SQLException {
                    long startTime = System.nanoTime();

                    try {
                        return super.getConnection();
                    } finally {
                        metricsCollectorProvider.getObject()
                                .recordConnectionWaitTime(System.nanoTime() - startTime);
                    }
                }

                @Override
                public Connection getConnection(String username, String password)
                        throws SQLException {
                    long startTime = System.nanoTime();

                    try {
                        return super.getConnection(username, password);
                    } finally {
                        metricsCollectorProvider.getObject()
                                .recordConnectionWaitTime(System.nanoTime() - startTime);
                    }
                }
            };
        }

        return bean;
    }
}
