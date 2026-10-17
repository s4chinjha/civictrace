package com.civictrace.analytics;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public AnalyticsResponses.Summary summary() {
        return analyticsService.summary();
    }

    @GetMapping("/trends")
    public List<AnalyticsResponses.TrendPoint> trends(@RequestParam(defaultValue = "30") int days) {
        return analyticsService.trends(days);
    }

    @GetMapping("/hotspots")
    public List<AnalyticsResponses.Hotspot> hotspots() {
        return analyticsService.hotspots();
    }
}
