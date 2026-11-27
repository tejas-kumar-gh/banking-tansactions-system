package com.banking.banking_transaction_system.controller;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DatabaseConnectionController {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseConnectionController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/health/db")

    
    public Map<String, Object> checkDatabase() {

        Integer result = jdbcTemplate.queryForObject(
                "SELECT 1", Integer.class
        );

        return Map.of(
                "status", "UP",
                "database", "MySQL",
                "connection", result
        );
    }
}
