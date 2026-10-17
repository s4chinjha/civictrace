package com.civictrace.common.health;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    private final JdbcTemplate jdbcTemplate;

    public HealthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public HealthStatus check() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return new HealthStatus("UP", "UP");
        } catch (RuntimeException exception) {
            return new HealthStatus("DOWN", "DOWN");
        }
    }
}
