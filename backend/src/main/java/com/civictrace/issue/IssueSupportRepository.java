package com.civictrace.issue;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IssueSupportRepository extends JpaRepository<IssueSupport, UUID> {
    long countByIssueId(UUID issueId);

    boolean existsByIssueIdAndUserId(UUID issueId, UUID userId);
}
