CREATE UNIQUE INDEX uq_assignments_one_active_issue
    ON assignments(issue_id)
    WHERE ended_at IS NULL;
