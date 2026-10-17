# AGENTS.md — CivicTrace

## 1. Project Identity

**Project:** CivicTrace  
**Purpose:** College semester project — civic issue reporting, tracking, and analytics platform.

- **Local repository:** `/home/sachinkrjha/Projects/civictrace`
- **GitHub repository:** https://github.com/s4chinjha/civictrace.git
- **Primary goal:** Build a working MVP locally as quickly as possible, then deploy it for ₹0 using free hosting tiers.
- **Development approach:** Implement incrementally, run the application and tests, and verify actual behavior. Do not stop at planning.

Always work inside the existing local repository. Do not create another project directory or clone the repository into a nested folder.

## 2. System Environment

The development environment has already been inspected.

| Component | Configuration |
|---|---|
| OS | Fedora Linux 43 |
| Java | 21 |
| Maven | 3.9.11 |
| Node.js | 22.22.2 |
| npm | 10.9.7 |
| Docker | 29.6.2 |
| Docker Compose | v5 |
| Database | PostgreSQL |
| CivicTrace PostgreSQL port | 5435 |
| Existing PostgreSQL containers | Ports 5433 and 5434 |
| Frontend development URL | `http://localhost:5173` |
| Backend URL | `http://localhost:8080` |
| Health endpoint | `http://localhost:8080/api/health` |

### Environment rules

- Use Java 21 and the installed Node.js environment.
- Use port 5435 for CivicTrace PostgreSQL.
- Check port availability before starting services.
- Never stop, delete, or modify unrelated containers.
- Inspect the existing environment before installing tools or changing system configuration.
- Do not assume any planned command has succeeded without executing it.

## 3. Repository and Git Rules

Before modifying files:

1. Read this file and inspect any additional repository instructions.
2. Run `git status` and inspect the current branch.
3. Inspect existing files and recent commits.
4. Verify the configured Git remote and its URL.
5. Preserve existing work and history.

Expected GitHub remote:

`https://github.com/s4chinjha/civictrace.git`

- If `origin` already exists, inspect it before changing anything.
- If the local repository is not initialized, inspect its contents before initializing Git.
- If local and remote histories differ, investigate and reconcile them safely.
- Never force-push, discard user changes, or overwrite remote history.
- Do not push automatically. Review the diff and test results first.
- Keep `.env`, credentials, passwords, tokens, and production secrets out of Git.
- Commit coherent, tested changes when appropriate.
- Do not claim a push or commit succeeded unless it actually did.

## 4. Product Requirements

CivicTrace allows residents to report civic problems such as potholes, broken streetlights, overflowing garbage bins, damaged roads, and water leaks.

The application should support:

- Public issue browsing, search, and filters.
- Resident registration, login, and issue submission.
- Issue details, locations, support counts, and status timelines.
- Staff review, assignment, and status management.
- Nearby duplicate suggestions.
- An interactive issue map.
- Database-backed analytics and charts.
- Real-time updates using WebSockets.
- Secure role-based authorization.
- Repeatable local setup and free deployment documentation.

The application should be polished enough for a college demonstration and simple enough to explain during a viva.

## 5. Technology Stack

### Frontend

- React
- TypeScript
- Vite
- React Router
- TanStack Query
- Recharts
- Leaflet
- STOMP client for WebSockets
- Vitest and React Testing Library
- Tailwind CSS or lightweight custom CSS

### Backend

- Java 21
- Spring Boot
- Maven
- Spring Web
- Spring Data JPA
- Spring Security
- Bean Validation
- PostgreSQL JDBC driver
- Flyway
- Spring WebSocket/STOMP
- JUnit and Spring Boot testing support

Use compatible, stable dependency versions. Prefer existing dependencies over introducing alternatives without a clear reason.

### Database

PostgreSQL is the source of truth.

- Use Flyway migrations.
- Use UUID identifiers where appropriate.
- Enforce foreign keys, unique constraints, and useful indexes.
- Use deterministic seed data for demonstrations.
- Keep credentials in environment variables, not source code.

## 6. Architecture

Use a modular monolith. Do not introduce microservices, Kubernetes, Redis, Kafka, or other infrastructure without a demonstrated need.

Suggested backend packages:

    com.civictrace
    ├── auth/
    ├── user/
    ├── issue/
    ├── support/
    ├── workflow/
    ├── analytics/
    ├── realtime/
    ├── common/
    └── config/

Keep controllers thin, put business rules in services, validate requests, and use DTOs rather than exposing JPA entities directly.

## 7. Authentication and Security

Use Spring Security with secure session cookies and CSRF protection.

Roles:

- `RESIDENT`
- `STAFF`

Requirements:

- Hash passwords using an appropriate password encoder.
- Enforce authorization in the backend.
- Restrict assignments and workflow administration to staff.
- Prevent residents from changing protected data or performing staff actions.
- Configure CORS for the actual frontend origin.
- Configure WebSocket authentication and authorization.
- Never log passwords, session identifiers, or secrets.
- Never store plaintext passwords.
- Validate and normalize user input.

Optional demo staff initialization is controlled by:

- `APP_SEED_DEMO`
- `DEMO_STAFF_EMAIL`
- `DEMO_STAFF_PASSWORD`

Demo seeding must be disabled by default in production. Never hardcode production credentials or print passwords in logs.

## 8. Issue Workflow

Suggested categories:

- `ROADS`
- `STREETLIGHTS`
- `WASTE`
- `WATER`
- `DRAINAGE`
- `OTHER`

Suggested statuses:

- `REPORTED`
- `UNDER_REVIEW`
- `IN_PROGRESS`
- `RESOLVED`
- `REJECTED`
- `DUPLICATE`

Enforce valid status transitions in the backend.

Every meaningful transition should record the actor, previous status, new status, timestamp, and optional explanation.

Persist the status change and audit record consistently in a database transaction. Publish real-time events only after successful persistence.

Duplicate suggestions are advisory. Never automatically merge unrelated reports or describe community reports as officially verified without evidence.

## 9. Database Entities

Start with:

- `users`
- `issues`
- `issue_supports`
- `issue_updates`
- `assignments`
- `issue_relations`

Important constraints:

- Unique user email.
- Unique `(issue_id, user_id)` in `issue_supports`.
- Foreign keys for relational integrity.
- Indexes for commonly filtered status, category, and creation-time fields.
- Consistent `resolved_at` handling for resolution analytics.
- Separate audit history from the current issue status.

Keep the schema focused on actual MVP requirements.

## 10. Application Features and Priority

Implement in this order:

### Phase 1 — Working foundation

- Scaffold frontend and backend.
- Configure PostgreSQL on port 5435.
- Add Flyway migrations and environment configuration.
- Add the health endpoint.
- Verify database connectivity and frontend/backend startup.

### Phase 2 — Authentication

- Registration, login, logout, and current-user endpoint.
- Secure session management and CSRF protection.
- Resident/staff authorization.
- Authentication and permission tests.

### Phase 3 — Core issue workflow

- Create, browse, filter, and view issues.
- Resident support with duplicate prevention.
- Assignments and status transitions.
- Persistent status timeline.
- Resident and staff dashboards.

### Phase 4 — Differentiating features

- Nearby same-category duplicate suggestions.
- Leaflet map with issue markers and attribution.
- Database-backed analytics and charts.
- Issue aging, resolution-time trends, and geographic hotspots.

### Phase 5 — Real-time updates

- Spring WebSocket/STOMP endpoint at `/ws`.
- Public issue events through `/topic/issues`.
- Publish only after successful database commits.
- Reconnect and refresh state after disconnection.
- Do not expose sensitive user information.

### Phase 6 — Verification and deployment

- Run tests and builds.
- Fix errors and security issues.
- Verify the complete workflow locally.
- Add the backend Dockerfile and production configuration.
- Prepare free deployment instructions.

Do not restart planning when implementation can proceed. Work incrementally, verify each phase, and continue autonomously unless a genuine blocker requires user input.

## 11. Local Development

Intended commands:

Start PostgreSQL:

    docker compose up -d db

Start the backend:

    cd backend
    ./mvnw spring-boot:run

Start the frontend:

    cd frontend
    npm install
    npm run dev

Intended URLs:

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- Health: `http://localhost:8080/api/health`

These are planned commands and URLs until verified against the actual implementation.

Provide:

- `docker-compose.yml`
- `.env.example`
- `.gitignore`
- Flyway migrations
- Repeatable seed data
- README with prerequisites, setup, and troubleshooting

Never commit real `.env` files or secrets.

## 12. Testing and Quality

Run actual checks after meaningful changes.

Backend:

    ./mvnw test
    ./mvnw package

Frontend:

    npm test
    npm run build

Adapt commands to the scripts actually present in the project.

Test at least:

- Registration, login, and authorization.
- Issue creation and retrieval.
- Duplicate support prevention.
- Valid and invalid status transitions.
- Status history persistence.
- Analytics against known records and empty datasets.
- Frontend loading, error, and filtering states.
- WebSocket updates and reconnection where practical.

Do not disable tests or weaken security to get a passing build. Report actual results and distinguish untested features from verified ones.

## 13. UI Requirements

Build a responsive, polished civic-tech interface with:

- Landing page.
- Public issue explorer.
- Map and list views.
- Issue details and timeline.
- Login and registration.
- Resident dashboard.
- Staff dashboard.
- Analytics dashboard.

Use real API data, clear status labels, useful loading and empty states, validation messages, and responsive layouts.

Avoid dead buttons, fabricated metrics, unnecessary animations, and visual polish that delays core functionality.

Label seeded demonstration data clearly.

## 14. Free Deployment

Deploy only after local verification.

Target:

- Frontend: Render Static Site or Vercel.
- Backend: Render Web Service using Docker.
- Database: a suitable free PostgreSQL provider, such as Neon or Supabase.

Requirements:

- ₹0 target; no paid APIs, paid domain, or mandatory paid services.
- Verify current free-tier limits before choosing providers.
- Do not use Render's free PostgreSQL for a semester-long dataset because that free database expires after 30 days.
- Configure production environment variables, CORS, HTTPS cookies, WebSockets, migrations, and health checks.
- Document service sleeping, storage limits, quotas, and backup procedures.
- Do not perform account changes or deploy using credentials without appropriate authorization.

## 15. Agent Operating Rules

- Work directly in `/home/sachinkrjha/Projects/civictrace`.
- Inspect before modifying.
- Preserve existing work and Git history.
- Implement rather than merely describe commands.
- Run tests and builds instead of assuming success.
- Fix failures before proceeding.
- Avoid unnecessary dependencies, abstractions, and infrastructure.
- Make reasonable decisions without repeatedly asking for approval.
- Ask for input only when a genuine blocker requires it.
- Never expose or commit secrets.
- Never make destructive changes to unrelated services or files.
- Keep documentation synchronized with the actual code.
- Do not claim an implementation, test, commit, push, or deployment succeeded without verification.

## 16. Definition of Done

The local MVP is ready when:

- Frontend and backend start successfully.
- PostgreSQL works on port 5435.
- Flyway migrations succeed.
- Residents can register, log in, submit issues, and view them.
- Residents can support each issue only once.
- Staff can assign and update issues.
- Status history is persisted and visible.
- Map and analytics use real backend data.
- WebSocket functionality works if implemented.
- Authorization and validation tests pass.
- README setup instructions have been verified.
- No essential UI actions are broken.
- No secrets or unrelated destructive changes are present.
- Free deployment configuration and limitations are documented.

At completion, report implemented features, verified commands, actual test results, local URLs, Git status, remote configuration, known limitations, and deployment steps.

**Primary objective: build a working end-to-end local MVP as quickly as possible, then verify and deploy it. Do not replace implementation with repeated planning.**
