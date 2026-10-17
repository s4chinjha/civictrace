package com.civictrace.issue;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IssueRelationRepository extends JpaRepository<IssueRelation, UUID> {
}
