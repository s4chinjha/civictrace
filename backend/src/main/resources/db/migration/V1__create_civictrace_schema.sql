CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('RESIDENT', 'STAFF')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE issues (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id UUID NOT NULL REFERENCES users(id),
    title VARCHAR(160) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(30) NOT NULL CHECK (category IN ('ROADS', 'STREETLIGHTS', 'WASTE', 'WATER', 'DRAINAGE', 'OTHER')),
    status VARCHAR(30) NOT NULL CHECK (status IN ('REPORTED', 'UNDER_REVIEW', 'IN_PROGRESS', 'RESOLVED', 'REJECTED', 'DUPLICATE')),
    latitude NUMERIC(9, 6) NOT NULL CHECK (latitude BETWEEN -90 AND 90),
    longitude NUMERIC(9, 6) NOT NULL CHECK (longitude BETWEEN -180 AND 180),
    location_label VARCHAR(240),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMPTZ
);

CREATE TABLE issue_supports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    issue_id UUID NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_issue_support_user UNIQUE (issue_id, user_id)
);

CREATE TABLE issue_updates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    issue_id UUID NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    actor_id UUID NOT NULL REFERENCES users(id),
    old_status VARCHAR(30) CHECK (old_status IS NULL OR old_status IN ('REPORTED', 'UNDER_REVIEW', 'IN_PROGRESS', 'RESOLVED', 'REJECTED', 'DUPLICATE')),
    new_status VARCHAR(30) NOT NULL CHECK (new_status IN ('REPORTED', 'UNDER_REVIEW', 'IN_PROGRESS', 'RESOLVED', 'REJECTED', 'DUPLICATE')),
    comment VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    issue_id UUID NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    staff_id UUID NOT NULL REFERENCES users(id),
    assigned_by UUID NOT NULL REFERENCES users(id),
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMPTZ
);

CREATE TABLE issue_relations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_issue_id UUID NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    related_issue_id UUID NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    relation_type VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_issue_relation_distinct CHECK (source_issue_id <> related_issue_id),
    CONSTRAINT uq_issue_relation UNIQUE (source_issue_id, related_issue_id, relation_type)
);

CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_issues_status ON issues(status);
CREATE INDEX idx_issues_category ON issues(category);
CREATE INDEX idx_issues_created_at ON issues(created_at DESC);
CREATE INDEX idx_issues_coordinates ON issues(latitude, longitude);
CREATE INDEX idx_issue_updates_issue_created ON issue_updates(issue_id, created_at DESC);
CREATE INDEX idx_assignments_issue_active ON assignments(issue_id) WHERE ended_at IS NULL;
