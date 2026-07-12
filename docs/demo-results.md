# Demonstration Results

Verified locally on 2026-07-12 with Java 21.0.11, Docker Desktop, and PostgreSQL 17.

- `docker compose up -d --build`: PASS; the application and database both reached `healthy`.
- Flyway: PASS; all four production migrations ran and Hibernate validated the migrated schema.
- Create: PASS; `POST /api/v1/appointments` returned HTTP 201 and a `Location` header.
- Retrieve: PASS; the URI returned the same confirmed appointment and all required associations.
- List: PASS; the dealership-filtered collection included the persisted booking.
- Restart persistence: PASS; after restarting the application container, appointment
  `9bd5b7f5-f6ad-4d1c-88d5-c0e4cf80a97c` remained retrievable as `CONFIRMED`.
- Readiness and OpenAPI: PASS (`UP` and HTTP 200 respectively).

The demonstrated request used the deterministic development records documented in `README.md` and a
future UTC start instant. The persistent volume was retained during restart; reviewers can use a new
start instant and idempotency key when repeating the demonstration.
