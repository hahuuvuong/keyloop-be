# Quickstart Validation: Unified Service Scheduler

## Prerequisites

- Java 21
- Docker with Compose support (also required by Testcontainers)
- No locally installed Maven or PostgreSQL is required

## Verify the Build

On Windows run `mvnw.cmd verify`; on Unix-like systems run `./mvnw verify`. Expected result: unit,
PostgreSQL integration, HTTP, Flyway/JPA validation, and concurrency tests pass. The concurrency test
starts with exactly one eligible technician and bay, synchronizes two overlapping HTTP requests,
observes at most one 201 and a 409 loser, then finds exactly one appointment in PostgreSQL.

## Start Locally

Run `docker compose up --build`. Compose starts pinned-major PostgreSQL and application images, waits
for database health, runs Flyway, validates mappings, loads only explicitly enabled development seed
data, and waits for application readiness. No production credentials belong in the repository.

Expected locations: Swagger UI `/swagger-ui.html`; OpenAPI `/v3/api-docs`; health
`/actuator/health`; liveness `/actuator/health/liveness`; readiness `/actuator/health/readiness`;
metrics `/actuator/metrics`.

## Validate Scenario A

Use deterministic seed identifiers documented by the seed mechanism and `contracts/openapi.yaml`.

1. POST `/api/v1/appointments` with a unique `Idempotency-Key`, known references, and offset-bearing
   start time. Expect 201, Location, Confirmed status, all resource URIs, and calculated UTC end time.
2. GET the returned Location and verify the same associations and interval.
3. Repeat the identical POST/key. Expect HTTP 201 with the original representation and Location and no second row.
4. Reuse the key with different content. Expect HTTP 400 `IDEMPOTENCY_KEY_REUSED` and no new row.
5. POST an unavailable overlap. Expect 409 `BOOKING_CONFLICT`.
6. POST at exactly the previous end time with a new key. Expect 201.
7. Submit unknown and inconsistent references. Expect distinct 404/400 ProblemDetail codes.
8. List with dealership, UTC range, status, paging, and allowlisted sorting; verify deterministic matches.
9. If implemented, call availability preview and treat it as advisory; POST remains authoritative.

Every response returns or propagates `X-Correlation-ID`. Problems include stable code, title, detail,
path, timestamp, and correlation ID without SQL, constraints, stack traces, or personal data.

## Inspect Operability and Delivery

Exercise success, expected conflict, and invalid requests; verify booking counters and duration timer.
Follow a request through structured logs by correlation ID and confirm no personal data. Production
schema migrations and development seed data use separate locations or commands. Review the system
design, diagrams, decisions, assumptions, limitations, demo, and AI Collaboration Narrative, including
manual verification and ownership evidence.
