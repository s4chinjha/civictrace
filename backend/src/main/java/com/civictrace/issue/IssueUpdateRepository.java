package com.civictrace.issue;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IssueUpdateRepository extends JpaRepository<IssueUpdate, UUID> {
    List<IssueUpdate> findByIssueIdOrderByCreatedAtAsc(UUID issueId);
}
