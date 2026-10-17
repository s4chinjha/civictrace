package com.civictrace.analytics;

import java.time.LocalDate;
import java.util.List;

public final class AnalyticsResponses {

    private AnalyticsResponses() {
    }

    public record Summary(
            long totalReports,
            long openReports,
            long resolvedReports,
            double averageResolutionHours,
            List<CategoryCount> byCategory,
            List<AgingBucket> agingOpenReports
    ) {
    }

    public record CategoryCount(String category, long count) {
    }

    public record AgingBucket(String bucket, long count) {
    }

    public record TrendPoint(LocalDate date, long count) {
    }

    public record Hotspot(String cell, double latitude, double longitude, long count) {
    }
}
