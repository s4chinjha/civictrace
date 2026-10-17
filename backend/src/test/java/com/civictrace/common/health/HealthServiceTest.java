package com.civictrace.common.health;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HealthServiceTest {

    @Test
    void reportsDatabaseUpWhenDatabaseQuerySucceeds() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);

        HealthStatus status = new HealthService(jdbcTemplate).check();

        assertThat(status.status()).isEqualTo("UP");
        assertThat(status.database()).isEqualTo("UP");
    }

    @Test
    void reportsDatabaseDownWhenDatabaseQueryFails() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class))
                .thenThrow(new IllegalStateException("database unavailable"));

        HealthStatus status = new HealthService(jdbcTemplate).check();

        assertThat(status.status()).isEqualTo("DOWN");
        assertThat(status.database()).isEqualTo("DOWN");
    }
}
