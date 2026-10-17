package com.civictrace.analytics;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AnalyticsService {

    private final JdbcTemplate jdbcTemplate;

    public AnalyticsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public AnalyticsResponses.Summary summary() {
        long total = count("SELECT COUNT(*) FROM issues");
        long open = count("SELECT COUNT(*) FROM issues WHERE status IN ('REPORTED', 'UNDER_REVIEW', 'IN_PROGRESS')");
        long resolved = count("SELECT COUNT(*) FROM issues WHERE status = 'RESOLVED'");
        Double average = jdbcTemplate.queryForObject(
                "SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (resolved_at - created_at)) / 3600), 0) FROM issues WHERE resolved_at IS NOT NULL",
                Double.class
        );
        List<AnalyticsResponses.CategoryCount> byCategory = jdbcTemplate.query(
                "SELECT category, COUNT(*) FROM issues GROUP BY category ORDER BY COUNT(*) DESC",
                (rs, rowNum) -> new AnalyticsResponses.CategoryCount(rs.getString(1), rs.getLong(2))
        );
        List<AnalyticsResponses.AgingBucket> aging = jdbcTemplate.query(
                """
                SELECT bucket, count FROM (
                    SELECT '0–7 days' AS bucket, 1 AS bucket_order, COUNT(*) AS count
                    FROM issues WHERE status IN ('REPORTED', 'UNDER_REVIEW', 'IN_PROGRESS') AND created_at >= CURRENT_TIMESTAMP - INTERVAL '7 days'
                    UNION ALL
                    SELECT '8–30 days', 2, COUNT(*)
                    FROM issues WHERE status IN ('REPORTED', 'UNDER_REVIEW', 'IN_PROGRESS') AND created_at < CURRENT_TIMESTAMP - INTERVAL '7 days' AND created_at >= CURRENT_TIMESTAMP - INTERVAL '30 days'
                    UNION ALL
                    SELECT '31+ days', 3, COUNT(*)
                    FROM issues WHERE status IN ('REPORTED', 'UNDER_REVIEW', 'IN_PROGRESS') AND created_at < CURRENT_TIMESTAMP - INTERVAL '30 days'
                ) aging ORDER BY bucket_order
                """,
                (rs, rowNum) -> new AnalyticsResponses.AgingBucket(rs.getString("bucket"), rs.getLong("count"))
        );
        return new AnalyticsResponses.Summary(total, open, resolved, average == null ? 0 : round(average), byCategory, aging);
    }

    @Transactional(readOnly = true)
    public List<AnalyticsResponses.TrendPoint> trends(int days) {
        int safeDays = Math.max(7, Math.min(days, 365));
        return jdbcTemplate.query(
                "SELECT DATE(created_at) AS day, COUNT(*) AS count FROM issues WHERE created_at >= CURRENT_DATE - (? * INTERVAL '1 day') GROUP BY DATE(created_at) ORDER BY day",
                (rs, rowNum) -> new AnalyticsResponses.TrendPoint(rs.getObject("day", LocalDate.class), rs.getLong("count")),
                safeDays
        );
    }

    @Transactional(readOnly = true)
    public List<AnalyticsResponses.Hotspot> hotspots() {
        return jdbcTemplate.query(
                """
                SELECT CONCAT(ROUND(FLOOR(latitude * 10) / 10.0, 1), ', ', ROUND(FLOOR(longitude * 10) / 10.0, 1)) AS cell,
                       AVG(latitude) AS latitude, AVG(longitude) AS longitude, COUNT(*) AS count
                FROM issues GROUP BY FLOOR(latitude * 10), FLOOR(longitude * 10) ORDER BY count DESC LIMIT 30
                """,
                (rs, rowNum) -> new AnalyticsResponses.Hotspot(
                        rs.getString("cell"), rs.getDouble("latitude"), rs.getDouble("longitude"), rs.getLong("count")
                )
        );
    }

    private long count(String sql) {
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count == null ? 0 : count;
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
