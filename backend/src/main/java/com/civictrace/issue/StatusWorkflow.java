package com.civictrace.issue;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class StatusWorkflow {

    private static final Map<IssueStatus, Set<IssueStatus>> ALLOWED = new EnumMap<>(Map.of(
            IssueStatus.REPORTED, EnumSet.of(IssueStatus.UNDER_REVIEW, IssueStatus.REJECTED, IssueStatus.DUPLICATE),
            IssueStatus.UNDER_REVIEW, EnumSet.of(IssueStatus.IN_PROGRESS, IssueStatus.REJECTED, IssueStatus.DUPLICATE),
            IssueStatus.IN_PROGRESS, EnumSet.of(IssueStatus.RESOLVED, IssueStatus.REJECTED, IssueStatus.DUPLICATE),
            IssueStatus.RESOLVED, EnumSet.noneOf(IssueStatus.class),
            IssueStatus.REJECTED, EnumSet.noneOf(IssueStatus.class),
            IssueStatus.DUPLICATE, EnumSet.noneOf(IssueStatus.class)
    ));

    private StatusWorkflow() {
    }

    public static boolean isValid(IssueStatus current, IssueStatus next) {
        return current != null && next != null && ALLOWED.getOrDefault(current, Set.of()).contains(next);
    }
}
