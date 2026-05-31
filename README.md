# company-registration-api

REST API to register a company asynchronously, with API-key authentication, `clientRequestId` dedup on submit, and background duplicate detection (DB lookup + vector search placeholder).

## Demo guide

Presentation-friendly overview (purpose, flows, status diagram, tech stack): **[docs/demo.html](docs/demo.html)** — open in a browser for demos.

**Documentation maintenance:** when you change API behavior, architecture, or stack details, update **both** this README and `docs/demo.html` so they stay aligned.

## Stack

- Java 25
- Spring Boot 4.0.6
- Spring Web MVC, Validation, Data JPA, Jackson, Actuator (Micrometer)
- Project Reactor (`Mono`) for concurrent registration lookups
- H2 (in-memory database for development)
- HTTPS on port 8443

## Requirements

- JDK 25+
- Maven 3.6.3+

## Project layout

Base package: `com.jackie.companyregistration`

| Package | Responsibility |
|---------|----------------|
| `controller` | REST endpoints |
| `service` | Registration workflow, company persistence |
| `service.lookup` | `CompanyDbLookupService` + vector-search orchestration (Reactor) |
| `security` | API-key filter and client context |
| `config` | Async executor, API filter registration |
| `config.seeder` | Disabled Java seeders (`.java.disabled`); use `db/ddl/data.sql` instead |
| `model` / `repository` / `dto` / `exception` | Domain, persistence, API types |

## Configuration

Environment-specific values live in `env.yaml` at the project root (not committed). For local in-memory H2, copy `env.h2.yaml`:

```bash
cp env.h2.yaml env.yaml
```

For PostgreSQL (e.g. Neon), set `app.db.url`, `username`, `password`, and `driver-class-name: org.postgresql.Driver` in `env.yaml`.

| Key | Description |
|-----|-------------|
| `app.db.url` | JDBC connection URL |
| `app.db.driver-class-name` | JDBC driver (`org.h2.Driver` or `org.postgresql.Driver`) |
| `app.db.username` | Database username |
| `app.db.password` | Database password |
| `app.ssl.keystore-path` | Keystore location (`classpath:...` or `file:...`) |
| `app.ssl.keystore-password` | Keystore password |

Application settings in `src/main/resources/application.yaml`:

| Key | Default | Description |
|-----|---------|-------------|
| `app.vector-search.delay-ms` | `1000` | Placeholder vector-search delay (ms). Set to `0` in tests. |

`application.yaml` imports `optional:file:./env.yaml` and falls back to in-memory H2 / classpath keystore defaults when the file is absent (for example during tests).

## Database schema

DDL script: `src/main/resources/db/ddl/schema.sql`

Seed / reference data: `src/main/resources/db/ddl/data.sql` (clients, `registration_request_statuses`). Uses `INSERT ... ON CONFLICT` (H2 2.x and PostgreSQL). Run after schema when not using Java seeders.

Tear-down: `src/main/resources/db/ddl/drop.sql` drops all application tables (H2 2.x and PostgreSQL). Use before re-running `schema.sql` on a non-empty database.

Run scripts against your database before pointing the app at a persistent store (PostgreSQL, MySQL, etc.). Hibernate `ddl-auto: update` is enabled for local H2 development; use the scripts when you manage schema and data explicitly in other environments.

| Table | Purpose |
|-------|---------|
| `clients` | API client id and API key; `created_at` / `updated_at` timestamps |
| `registration_request_statuses` | Lookup table for registration request statuses; seeded from `RequestStatus` enum on startup |
| `companies` | Registered companies; `status` (`ACTIVE` / `INACTIVE`); unique `registration_number`; unique `name` among ACTIVE rows only (`uk_companies_name_active`); timestamps |
| `company_name_history` | Append-only log of company names (`name`, `changed_at`); order by timestamp to see renames |
| `registration_requests` | Async job per client; `status_code` → `registration_request_statuses`; `client_id` → `clients`; index `idx_registration_requests_pending_reg_created` for scheduled polling |
| `registration_request_status_history` | Append-only log of status transitions per request |

## Authentication

All `/api/**` endpoints require an API key in the `X-API-Key` header. Valid keys are stored in the `clients` table (`client_id`, `api_key`).

On success, the authenticated `client_id` is attached to the request (`ClientContext`) for throttling or auditing.

Dev clients (see `db/ddl/data.sql`):

| `client_id` | `api_key` |
|-------------|-----------|
| `dev-client` | `dev-api-key-change-me` |
| `partner-client` | `partner-api-key-change-me` |
| `internal-client` | `internal-api-key-change-me` |

Add more clients by inserting rows into `clients`. Reload the in-memory API key cache without restart:

```bash
curl -k -X POST https://localhost:8443/api/admin/cache/clients/reload \
  -H "X-Admin-Key: your-admin-secret"
```

Response: `{"clientCount":3}`

Set `app.admin.api-key` in `env.yaml` (or env var `APP_ADMIN_API_KEY`). Admin routes use `X-Admin-Key`, not `X-API-Key`. If unset, admin endpoints return **503**.

**Multiple pods:** each JVM has its own cache. A load-balanced POST hits **one** pod only. Options:

- Call reload on **each pod** (pod IP/DNS, or `kubectl exec` + localhost curl per pod)
- Run a **Kubernetes Job** that curls every pod endpoint after client changes
- Add **scheduled refresh** on all instances (every pod polls `clients` on a timer — no orchestration needed)

## Run

```bash
mvn spring-boot:run
```

The API listens on `https://localhost:8443` (HTTPS only; port 8080 is not used).

## TLS keystore

Keystore path and password are configured in `env.yaml` (`app.ssl.keystore-path`, `app.ssl.keystore-password`).

A development self-signed keystore is at `src/main/resources/ssl/keystore.p12` (alias `company-registration-api`).

```yaml
app:
  ssl:
    keystore-path: file:/path/to/your/keystore.p12
    keystore-password: your-password
```

Regenerate a dev keystore:

```bash
keytool -genkeypair -alias company-registration-api -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore src/main/resources/ssl/keystore.p12 -validity 3650 \
  -storepass changeit -keypass changeit \
  -dname "CN=localhost, OU=Development, O=Example, L=Local, ST=NA, C=US"
```

## Architecture

### Submit path (synchronous)

1. Validate API key → resolve `client_id`
2. Same `clientRequestId` for this client → **200 OK**, existing internal `requestId` (`duplicate: true`). Reusing the id with a different payload → **400 Bad Request**.
3. Otherwise save `registration_requests` row (`PENDING`) → **202 Accepted**, process in background

No registration-number or company-name validation runs on submit. All company checks happen in the background worker.

**Double-submit protection:** the webapp sends a stable `clientRequestId` (UUID per submission) in the JSON body. The API enforces `UNIQUE (client_id, client_request_id)`.

**Scheduled processing (planned):** pending requests for the same `registrationNumber` are processed sequentially across all clients (oldest `created_at` first per registration number). Index `idx_registration_requests_pending_reg_created` on `(registration_number, created_at) WHERE status_code = 'PENDING'` supports that poll query.

### Background worker (`RegistrationRequestWorker`)

**Current:** each accepted request is processed immediately on a background executor (requests may run in parallel).

**Planned:** replace with a scheduled job that polls `PENDING` rows (see index + query in `schema.sql`) and processes the same `registrationNumber` sequentially across all clients.

Status flow: `PENDING` → `PROCESSING` → `COMPLETED` | `FAILED`

Each transition is recorded in `registration_request_status_history`. Allowed status codes are stored in `registration_request_statuses` (see `db/ddl/data.sql`; aligned with the `RequestStatus` enum).

Before insert or reject, `RegistrationLookupOrchestrator` runs **two lookups in parallel** (Project Reactor `Mono`):

```
                    ┌─ CompanyDbLookupService ── registration_number then name (ACTIVE only)
RegistrationRequest │
                    └─ VectorSearchService      ── name similarity (placeholder, 1s delay)
```

`CompanyDbLookupService` checks **ACTIVE** companies only. Inactive companies do not block reuse of their name.

| DB lookup result | Worker action |
|------------------|---------------|
| `registrationNumber` exists (ACTIVE), same name | Cancel vector search → `COMPLETED` (link existing company) |
| `registrationNumber` exists (ACTIVE), different name | Cancel vector search → `FAILED` (use update name API) |
| `registrationNumber` not found, exact name match (ACTIVE) | Cancel vector search → `FAILED` (name registered under another number) |
| No DB match | Wait for vector search → currently always no match → insert company → `COMPLETED` |

Vector logic lives in `VectorSearchService.findSimilarCompany()` (placeholder returns empty). When implemented, close matches will create a human-review task instead of auto-registering.

## API

Use `-k` with curl for the self-signed dev certificate.

### Submit registration

`POST /api/companies`

```bash
curl -k -X POST https://localhost:8443/api/companies \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-api-key-change-me" \
  -d "{\"clientRequestId\":\"7c9e6679-7425-40de-944b-e07fc1f90ae7\",\"registrationNumber\":\"REG-001\",\"name\":\"Acme Corp\"}"
```

Required field **`clientRequestId`**: generate once per form submission in the webapp (UUID) and reuse on retries or double-clicks. Replays with the same id and payload return the original internal `requestId`. Reusing an id with a different payload returns **400 Bad Request**.

**202 Accepted** — new request queued:

```json
{
  "requestId": 1,
  "clientRequestId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "status": "PENDING",
  "registrationNumber": "REG-001",
  "createdAt": "2026-05-30T12:00:00Z",
  "duplicate": false,
  "message": null,
  "company": null
}
```

**200 OK** — same `clientRequestId` replay (no new job):

```json
{
  "requestId": 1,
  "clientRequestId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "status": "PENDING",
  "registrationNumber": "REG-001",
  "createdAt": "2026-05-30T12:00:00Z",
  "duplicate": true,
  "message": "Duplicate request",
  "company": null
}
```

Use `GET /api/companies/requests/{requestId}` to poll until `COMPLETED` or `FAILED`. Conflicts (same `registrationNumber` with a different name, or same name under a different number) surface as `FAILED` on the request, not as **400** on submit.

### Check registration status

`GET /api/companies/requests/{requestId}`

```bash
curl -k https://localhost:8443/api/companies/requests/1 \
  -H "X-API-Key: dev-api-key-change-me"
```

**200 OK** — processing:

```json
{
  "requestId": 1,
  "clientRequestId": "client-req-001",
  "status": "PROCESSING",
  "registrationNumber": "REG-001",
  "companyName": "Acme Corp",
  "errorMessage": null
}
```

**200 OK** — completed:

```json
{
  "requestId": 1,
  "clientRequestId": "client-req-001",
  "status": "COMPLETED",
  "registrationNumber": "REG-001",
  "companyName": "Acme Corp",
  "errorMessage": null
}
```

**200 OK** — failed (e.g. reg# exists with different name, detected in worker):

```json
{
  "requestId": 2,
  "clientRequestId": "client-req-002",
  "status": "FAILED",
  "registrationNumber": "REG-001",
  "companyName": "Other Name",
  "errorMessage": "Registration number 'REG-001' is already registered with company name 'Acme Corp'. Use the update name API to change it."
}
```

**404 Not Found** — unknown `requestId` or request owned by another client.

### Update company name

`PUT /api/companies/{registrationNumber}`

Use this when the company is **ACTIVE** and the client needs to change the display name. `registrationNumber` stays the same. INACTIVE companies must be reactivated via `POST /api/companies` (register), not renamed here.

```bash
curl -k -X PUT https://localhost:8443/api/companies/REG-001 \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-api-key-change-me" \
  -d "{\"name\":\"Acme International\"}"
```

**200 OK:**

```json
{
  "id": 1,
  "registrationNumber": "REG-001",
  "name": "Acme International",
  "status": "ACTIVE"
}
```

Name changes are recorded in `company_name_history` (`name`, `changed_at`). Rows are ordered by `changed_at` to reconstruct the rename timeline.

**404 Not Found** — unknown `registrationNumber`. **409 Conflict** — company is INACTIVE, or new name already used by another ACTIVE company.

### HTTP status summary

| Status | When |
|--------|------|
| **202 Accepted** | New async registration queued |
| **200 OK** | Same `clientRequestId` replay on submit, or status poll success |
| **400 Bad Request** | Validation error, or same `clientRequestId` reused with a different payload |
| **401 Unauthorized** | Missing or invalid API key |
| **404 Not Found** | Unknown registration request or company (PUT name) |
| **409 Conflict** | PUT name: company INACTIVE, or new name already used by another ACTIVE company |

Registration conflicts (same reg# with different name, or same name under another reg#) return **`FAILED`** on the async request after worker processing, not **400** on submit.

**401 example:**

```json
{
  "message": "Missing API key"
}
```

## Metrics

Spring Boot Actuator + Micrometer expose request timing and pool stats at **`/actuator/*`** (same HTTPS port **8443**). These paths do **not** require `X-API-Key`; restrict exposure in production (firewall, separate port, or auth).

| Endpoint | Purpose |
|----------|---------|
| `GET /actuator/health` | Liveness (`UP` / `DOWN`) |
| `GET /actuator/metrics` | Metric names |
| `GET /actuator/metrics/http.server.requests` | Per-route latency (count, mean, max, p50/p95/p99) |

Examples:

```bash
curl -k https://localhost:8443/actuator/health
curl -k https://localhost:8443/actuator/metrics/http.server.requests
```

Also auto-exported: JVM memory/GC, Hikari pool (`hikaricp.connections.*`), Tomcat sessions. Percentiles for `http.server.requests` are enabled in `application.yaml`.

## Test

```bash
mvn test
```

Tests use `app.vector-search.delay-ms=0` and a synchronous task executor so async flows complete without waiting.

## H2 Console

When running locally: `https://localhost:8443/h2-console`
