package com.civictrace.issue;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {
    Optional<Assignment> findFirstByIssueIdAndEndedAtIsNullOrderByAssignedAtDesc(UUID issueId);
}
