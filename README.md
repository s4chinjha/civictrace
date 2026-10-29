# CivicTrace

CivicTrace is a civic issue reporting and tracking MVP. Residents can report problems, support existing reports, and follow a transparent status timeline. Staff can review, assign, and progress reports. The public explorer, map, and analytics use PostgreSQL-backed data.

## Stack

- **Frontend:** React, TypeScript, Vite, React Router, TanStack Query, Recharts, Leaflet/OpenStreetMap, STOMP
- **Backend:** Java 21, Spring Boot 3.5, Spring Web/Data JPA/Security/WebSocket, Bean Validation, Flyway
- **Database:** PostgreSQL 16
- **Authentication:** Spring Security session cookie with HttpOnly cookie and CSRF token cookie

## Prerequisites

- Java 21+
- Maven 3.9+ (or use the checked-in Maven wrapper)
- Node.js 22+ and npm
- Docker and Docker Compose

The dedicated database uses host port **5435** so it does not interfere with the existing PostgreSQL containers on ports 5433 and 5434.

## Run locally

From the repository root:

```bash
cp .env.example .env
# Change the demo passwords in .env if desired; never commit .env.
docker compose up -d db
docker compose ps
```

Start the backend in a second terminal. Sourcing `.env` makes the optional demo seed accounts and local database values available to Spring Boot:

```bash
cd backend
set -a
source ../.env
set +a
./mvnw spring-boot:run
```

Start the frontend in a third terminal:

```bash
cd frontend
npm install
npm run dev
```

Open:

- Frontend: http://localhost:5173
- Backend health: http://localhost:8080/api/health
- Actuator health: http://localhost:8080/actuator/health

Expected health response:

```json
{"status":"UP","database":"UP"}
```

Flyway applies migrations automatically when the backend starts. Do not manually create tables.

## Demo accounts

When `APP_SEED_DEMO=true`, the backend creates the accounts described by `DEMO_STAFF_EMAIL`, `DEMO_STAFF_PASSWORD`, `DEMO_RESIDENT_EMAIL`, and `DEMO_RESIDENT_PASSWORD`, plus three sample reports. The seed is idempotent and does nothing when reports already exist.

The example values are local-only placeholders. Set your own values in `.env`; credentials are never stored in source code or logs.

To reset only the CivicTrace database and regenerate sample data, remove the named Compose volume **after confirming that it contains no data you need**:

```bash
docker compose down
docker volume rm civictrace_civictrace-postgres-data
docker compose up -d db
```

## Tests and builds

Keep the dedicated database running for backend integration tests:

```bash
cd backend
./mvnw test
./mvnw package

cd ../frontend
npm test
npm run build
```

Backend tests cover health/migrations, authentication, role restrictions, duplicate support prevention, valid and invalid workflow transitions, and persistent status history. Frontend tests cover the application shell; the build type-checks all routes and components.

## Main routes

- `/` — public landing page
- `/issues` — searchable/filterable issue explorer with list/map views
- `/issues/:id` — issue details, support action, location, and timeline
- `/report` — resident report form with nearby duplicate suggestions
- `/dashboard` — resident submissions
- `/staff` — staff review, assignment, and status management
- `/analytics` — PostgreSQL-backed charts and hotspots
- `/login`, `/register` — authentication

## API overview

- `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/csrf`
- `GET /api/me`
- `GET /api/issues`, `POST /api/issues`, `GET /api/issues/{id}`
- `GET /api/issues/{id}/updates`, `POST /api/issues/{id}/support`
- `POST /api/issues/suggestions`
- `POST /api/issues/{id}/assignments`
- `PATCH /api/issues/{id}/status`, `POST /api/issues/{id}/updates`
- `GET /api/analytics/summary`, `/trends`, `/hotspots`
- WebSocket/STOMP endpoint: `/ws` for authenticated sessions; issue event topic: `/topic/issues` (payloads contain no resident names)
