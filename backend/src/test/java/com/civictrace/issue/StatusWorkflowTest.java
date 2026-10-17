package com.civictrace.issue;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatusWorkflowTest {

    @Test
    void permitsTheNormalReportToResolutionFlow() {
        assertThat(StatusWorkflow.isValid(IssueStatus.REPORTED, IssueStatus.UNDER_REVIEW)).isTrue();
        assertThat(StatusWorkflow.isValid(IssueStatus.UNDER_REVIEW, IssueStatus.IN_PROGRESS)).isTrue();
        assertThat(StatusWorkflow.isValid(IssueStatus.IN_PROGRESS, IssueStatus.RESOLVED)).isTrue();
    }

    @Test
    void rejectsSkippingWorkflowOrChangingTerminalStatuses() {
        assertThat(StatusWorkflow.isValid(IssueStatus.REPORTED, IssueStatus.RESOLVED)).isFalse();
        assertThat(StatusWorkflow.isValid(IssueStatus.RESOLVED, IssueStatus.IN_PROGRESS)).isFalse();
        assertThat(StatusWorkflow.isValid(IssueStatus.REJECTED, IssueStatus.IN_PROGRESS)).isFalse();
    }
}
