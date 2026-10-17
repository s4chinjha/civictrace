package com.civictrace.analytics;

import com.civictrace.auth.Role;
import com.civictrace.auth.User;
import com.civictrace.auth.UserRepository;
import com.civictrace.issue.Issue;
import com.civictrace.issue.IssueCategory;
import com.civictrace.issue.IssueRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AnalyticsIntegrationTest {

    @Autowired
    AnalyticsService analyticsService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    IssueRepository issueRepository;

    @Test
    void calculatesSummaryTrendsAndHotspotsFromPostgresRecords() {
        User reporter = userRepository.save(new User(
                "Analytics Test Resident",
                "analytics-" + UUID.randomUUID() + "@example.test",
                "not-used-in-this-test",
                Role.RESIDENT
        ));
        issueRepository.save(new Issue(
                reporter, "Analytics test report", "Known record for analytics verification.", IssueCategory.WATER,
                new BigDecimal("28.600000"), new BigDecimal("77.200000"), "Analytics test location"
        ));
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
