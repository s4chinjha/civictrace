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
- WebSocket/STOMP endpoint: `/ws`; public event topic: `/topic/issues`

## Deployment preparation

The backend can be built with `backend/Dockerfile`. Production configuration is environment-based:

- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- `APP_CORS_ALLOWED_ORIGINS`
- `SESSION_COOKIE_SECURE=true` and `SESSION_COOKIE_SAME_SITE=none` when frontend/backend are on separate HTTPS origins
- `APP_SEED_DEMO=false` unless a presentation database explicitly needs seed data

Deploy only after local verification. Use a free static frontend host, a free backend web-service tier, and a free PostgreSQL provider whose current limits and retention policy you have checked. Configure the backend health check as `/actuator/health`, run Flyway on startup, set the production frontend API/WebSocket URLs, and use HTTPS-compatible cookies. Free services may sleep, throttle, limit storage, or expire databases; export a database backup before a presentation.

### Current ₹0 deployment path (checked October 2026)

The repository includes `render.yaml` for the two Render services. It intentionally does **not** create a hosted database, because Render's Free Postgres currently expires after 30 days.

1. **Create the database on Neon Free.** Create one PostgreSQL project and copy its connection details. The current Neon Free documentation lists 100 projects, 10 branches per project, 100 compute-hours per project/month, 1 GB storage per project, 5 GB public transfer, and scale-to-zero after 5 minutes idle. Limits can change; confirm them in the Neon console before relying on them.
2. **Deploy the backend on Render.** Create a Blueprint from this repository or create a Docker Web Service manually. Use `backend` as the root directory, `backend/Dockerfile`, the Free plan, and `/actuator/health` as the health check. Set `SPRING_DATASOURCE_URL` to the Neon JDBC URL with `?sslmode=require`, plus the Neon username/password.
3. **Set backend environment variables.** Set `APP_CORS_ALLOWED_ORIGINS` to the final frontend URL, `SESSION_COOKIE_SECURE=true`, `SESSION_COOKIE_SAME_SITE=none`, and `APP_SEED_DEMO=false`. For a fresh presentation database, you may temporarily set `APP_SEED_DEMO=true` and provide demo credentials in the Render dashboard, then turn it off after the first successful boot.
4. **Deploy the frontend as a Render Static Site.** Use root directory `frontend`, build command `npm ci && npm run build`, and publish directory `dist`. Set `VITE_API_BASE_URL=https://<backend-service>.onrender.com` and `VITE_WS_URL=wss://<backend-service>.onrender.com/ws`.
5. **Close the CORS loop.** Replace the backend's `APP_CORS_ALLOWED_ORIGINS` with the actual static-site URL after its first deploy, then redeploy the backend. Test login, issue creation, map data, analytics, and a status update from the hosted frontend.
6. **Plan for free-tier behavior.** Render Free web services sleep after 15 minutes without inbound traffic and have 750 free instance-hours per workspace/month; the first request after sleep is slow. Neon compute sleeps after inactivity. The frontend reconnects STOMP and refetches REST state after reconnecting, so a cold start should recover.
7. **Back up the presentation data.** Before a demonstration, export the Neon database with `pg_dump` using the connection string. Do not put that connection string or any password in GitHub, `render.yaml`, or README files.

Vercel Hobby is also suitable for the static frontend, but its current terms are for personal/non-commercial projects and its quotas can pause usage when exceeded. Supabase Free is an alternative PostgreSQL host, currently limited to 500 MB, two active projects, and possible pausing after one week of inactivity. Neon is the recommended database for this semester demo because it avoids Render's 30-day database expiry.
