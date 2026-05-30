# company-registration-api

REST API to register a company asynchronously, with API-key authentication, idempotent submit, and concurrent duplicate detection (exact match + vector search placeholder).

## Stack

- Java 25
- Spring Boot 4.0.6
- Spring Web MVC, Validation, Data JPA, Jackson
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
| `service.lookup` | Exact-match + vector-search orchestration (Reactor) |
| `security` | API-key filter and client context |
| `config` | Async executor, client seeder, filter registration |
| `model` / `repository` / `dto` / `exception` | Domain, persistence, API types |

## Configuration

Environment-specific values live in `env.yaml` at the project root (not committed). Copy the template before first run:

```bash
cp env.yaml.example env.yaml
```

| Key | Description |
|-----|-------------|
| `app.db.url` | JDBC connection URL |
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

Run it against your database before pointing the app at a persistent store (PostgreSQL, MySQL, etc.). Hibernate `ddl-auto: update` is enabled for local H2 development; use the script when you manage schema explicitly in other environments.

| Table | Purpose |
|-------|---------|
| `clients` | API client id and API key (authentication; future rate limiting by `client_id`) |
| `companies` | Registered companies |
| `registration_requests` | Async job status per client (`requestId`, status, linked `company_id`) |

## Authentication

All `/api/**` endpoints require an API key in the `X-API-Key` header. Valid keys are stored in the `clients` table (`client_id`, `api_key`).

On success, the authenticated `client_id` is attached to the request (`ApiClientContext`) for throttling or auditing.

Dev client seeded on startup:

| `client_id` | `api_key` |
|-------------|-----------|
| `dev-client` | `dev-api-key-change-me` |

Add more clients by inserting rows into `clients`.

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
2. **Idempotent pre-check** before creating a new request:
   - Company already exists → **200 OK**, `duplicate: true`, full `company` (no new job)
   - In-flight request with same reg# + name → **200 OK**, existing `requestId`
3. Otherwise save `registration_requests` row (`PENDING`) → **202 Accepted**, process in background

### Background worker (`RegistrationRequestWorker`)

Status flow: `PENDING` → `PROCESSING` → `COMPLETED` | `FAILED`

Before insert or reject, `RegistrationLookupOrchestrator` runs **two lookups in parallel** (Project Reactor `Mono`):

```
                    ┌─ ExactMatchLookupService ── registrationNumber DB lookup
RegistrationRequest │
                    └─ VectorSearchService     ── name similarity (placeholder, 1s delay)
```

| Lookup result | Worker action |
|---------------|---------------|
| Exact match, same name | Cancel vector search → `COMPLETED` (link existing company) |
| Exact match, different name | Cancel vector search → `FAILED` |
| No exact match | Wait for vector search → currently always no match → insert company → `COMPLETED` |

Vector logic lives in `VectorSearchService.findSimilarCompany()` (placeholder returns empty). When implemented, return a matching `Company` and the orchestrator will treat it as an exact-match outcome.

## API

Use `-k` with curl for the self-signed dev certificate.

### Submit registration

`POST /api/companies`

```bash
curl -k -X POST https://localhost:8443/api/companies \
  -H "Content-Type: application/json" \
  -H "X-API-Key: dev-api-key-change-me" \
  -d "{\"registrationNumber\":\"REG-001\",\"name\":\"Acme Corp\"}"
```

**202 Accepted** — new request queued:

```json
{
  "requestId": 1,
  "status": "PENDING",
  "registrationNumber": "REG-001",
  "createdAt": "2026-05-30T12:00:00Z",
  "duplicate": false,
  "message": null,
  "company": null
}
```

**200 OK** — idempotent duplicate (company already registered; no new job):

```json
{
  "requestId": 1,
  "status": "COMPLETED",
  "registrationNumber": "REG-001",
  "createdAt": "2026-05-30T12:00:00Z",
  "duplicate": true,
  "message": "Company already registered",
  "company": {
    "id": 1,
    "registrationNumber": "REG-001",
    "name": "Acme Corp"
  }
}
```

Same `registrationNumber` with a **different name** also returns **200 OK**, `duplicate: true`, a descriptive `message`, and the existing `company`.

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
  "status": "PROCESSING",
  "registrationNumber": "REG-001",
  "companyName": "Acme Corp",
  "company": null,
  "errorMessage": null,
  "createdAt": "2026-05-30T12:00:00Z",
  "updatedAt": "2026-05-30T12:00:01Z"
}
```

**200 OK** — completed:

```json
{
  "requestId": 1,
  "status": "COMPLETED",
  "registrationNumber": "REG-001",
  "companyName": "Acme Corp",
  "company": {
    "id": 1,
    "registrationNumber": "REG-001",
    "name": "Acme Corp"
  },
  "errorMessage": null,
  "createdAt": "2026-05-30T12:00:00Z",
  "updatedAt": "2026-05-30T12:00:02Z"
}
```

**200 OK** — failed (e.g. reg# exists with different name, detected in worker):

```json
{
  "requestId": 2,
  "status": "FAILED",
  "registrationNumber": "REG-001",
  "companyName": "Other Name",
  "company": null,
  "errorMessage": "Company with registration number 'REG-001' already exists",
  "createdAt": "2026-05-30T12:00:00Z",
  "updatedAt": "2026-05-30T12:00:02Z"
}
```

**404 Not Found** — unknown `requestId` or request owned by another client.

### HTTP status summary

| Status | When |
|--------|------|
| **202 Accepted** | New async registration queued |
| **200 OK** | Idempotent duplicate on submit, or status poll success |
| **400 Bad Request** | Validation error (missing fields) |
| **401 Unauthorized** | Missing or invalid API key |
| **404 Not Found** | Unknown registration request |

**401 example:**

```json
{
  "message": "Missing API key"
}
```

## Test

```bash
mvn test
```

Tests use `app.vector-search.delay-ms=0` and a synchronous task executor so async flows complete without waiting.

## H2 Console

When running locally: `https://localhost:8443/h2-console`
