package com.civictrace.issue;

import com.civictrace.auth.Role;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class IssueResponses {

    private IssueResponses() {
    }

    public record Summary(
            UUID id,
            String title,
            String description,
            IssueCategory category,
            IssueStatus status,
            BigDecimal latitude,
            BigDecimal longitude,
            String locationLabel,
            String reporterName,
            Instant createdAt,
            Instant updatedAt,
            Instant resolvedAt,
            long supportCount,
            boolean supportedByCurrentUser,
            UUID assignedStaffId,
            String assignedStaffName
    ) {
    }

    public record Update(
            UUID id,
            UUID actorId,
            String actorName,
            Role actorRole,
            IssueStatus oldStatus,
            IssueStatus newStatus,
            String comment,
            Instant createdAt
    ) {
    }

    public record Detail(Summary issue, List<Update> updates) {
    }

    public record Page(
            List<Summary> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
    }

    public record Suggestion(
            UUID id,
            String title,
            IssueCategory category,
            IssueStatus status,
            BigDecimal latitude,
            BigDecimal longitude,
            String locationLabel,
            double distanceKm
    ) {
    }

    public record Assignment(String staffName, Instant assignedAt) {
    }
}
