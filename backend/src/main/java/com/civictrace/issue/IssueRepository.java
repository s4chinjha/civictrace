package com.civictrace.issue;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface IssueRepository extends JpaRepository<Issue, UUID> {

    @Query("""
            SELECT i FROM Issue i
            WHERE (:query = '' OR lower(i.title) LIKE lower(concat('%', :query, '%'))
                OR lower(i.description) LIKE lower(concat('%', :query, '%'))
                OR lower(coalesce(i.locationLabel, '')) LIKE lower(concat('%', :query, '%')))
              AND (:category IS NULL OR i.category = :category)
              AND (:status IS NULL OR i.status = :status)
            """)
    Page<Issue> search(@Param("query") String query,
                       @Param("category") IssueCategory category,
                       @Param("status") IssueStatus status,
                       Pageable pageable);

    @Query("""
            SELECT i FROM Issue i
            WHERE i.reporter.id = :reporterId
              AND (:status IS NULL OR i.status = :status)
            ORDER BY i.createdAt DESC
            """)
    Page<Issue> findByReporter(@Param("reporterId") UUID reporterId,
                               @Param("status") IssueStatus status,
                               Pageable pageable);

    @Query("""
            SELECT i FROM Issue i
            WHERE i.category = :category
              AND i.status IN :statuses
            """)
    List<Issue> findOpenByCategory(@Param("category") IssueCategory category,
                                   @Param("statuses") Collection<IssueStatus> statuses);

    long countByStatus(IssueStatus status);
}
