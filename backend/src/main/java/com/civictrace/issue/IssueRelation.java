package com.civictrace.issue;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "issue_relations")
public class IssueRelation {

    @Id
    @UuidGenerator
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_issue_id", nullable = false)
    private Issue sourceIssue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "related_issue_id", nullable = false)
    private Issue relatedIssue;

    @Column(name = "relation_type", nullable = false, length = 30)
    private String relationType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected IssueRelation() {
    }

    public IssueRelation(Issue sourceIssue, Issue relatedIssue, String relationType) {
        this.sourceIssue = sourceIssue;
        this.relatedIssue = relatedIssue;
        this.relationType = relationType;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
