package com.civictrace.realtime;

import com.civictrace.issue.IssueStatus;

import java.time.Instant;
import java.util.UUID;

public record IssueChangeEvent(
        String type,
        UUID issueId,
        String title,
        IssueStatus status,
        Instant changedAt
) {
}
