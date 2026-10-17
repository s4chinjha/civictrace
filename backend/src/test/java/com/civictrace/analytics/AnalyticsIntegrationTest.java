package com.civictrace.analytics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AnalyticsIntegrationTest {

    @Autowired
    AnalyticsService analyticsService;

    @Test
    void calculatesSummaryTrendsAndHotspotsFromPostgresRecords() {
        AnalyticsResponses.Summary summary = analyticsService.summary();

        assertThat(summary.totalReports()).isGreaterThanOrEqualTo(1);
        assertThat(summary.openReports()).isGreaterThanOrEqualTo(0);
        assertThat(summary.resolvedReports()).isGreaterThanOrEqualTo(0);
        assertThat(summary.byCategory()).isNotEmpty();
        assertThat(summary.agingOpenReports()).hasSize(3);
        assertThat(analyticsService.trends(30)).isNotEmpty();
        assertThat(analyticsService.hotspots()).isNotEmpty();
    }
}
