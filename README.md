# Unified Service Scheduler

Scenario A service for atomically scheduling dealership appointments without double-booking a
technician or service bay.

## Requirements

- Java 21
- Docker with Compose (required for PostgreSQL and integration tests)

## Build and test

Windows: `mvnw.cmd verify`. Unix: `./mvnw verify`. Tests use real PostgreSQL through Testcontainers;
H2 is not used. Flyway is the only schema authority and Hibernate runs with `ddl-auto=validate`.

## Run

Copy `.env.example` to `.env`, replace the demonstration password, then run
`docker compose up --build`. Development seed data is enabled only by the `dev` profile. Production
migrations are under `db/migration`; demonstration seed data is isolated under `db/dev-migration`.

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI: http://localhost:8080/v3/api-docs
- Health: http://localhost:8080/actuator/health
- Readiness: http://localhost:8080/actuator/health/readiness
- Metrics: http://localhost:8080/actuator/metrics

## Example booking

```bash
curl -i -X POST http://localhost:8080/api/v1/appointments \
  -H 'Content-Type: application/json' -H 'Idempotency-Key: demo-booking-001' \
  -d '{"customerId":"00000000-0000-0000-0000-000000000002","vehicleId":"00000000-0000-0000-0000-000000000003","dealershipId":"00000000-0000-0000-0000-000000000001","serviceTypeId":"00000000-0000-0000-0000-000000000005","startTime":"2030-01-01T10:00:00Z"}'
```

A successful response is 201 with Location. Replaying identical content/key returns the same 201,
Location, and representation without a duplicate. Changed content with the same key returns 400.
Canonical resource URIs in responses are identifiers and are not guaranteed dereferenceable in Scenario A.

## Scope and limitations

Authentication, UI, shifts, opening hours, cancellation actions, rescheduling, notifications, payments,
messaging, and cloud deployment are out of scope. See `docs/system-design.md` and
`docs/concurrency-decision.md`.
