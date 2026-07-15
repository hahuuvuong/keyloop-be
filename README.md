# Unified Service Scheduler

Scenario A service for atomically scheduling dealership appointments without double-booking a
technician or service bay.

## Requirements

- Java 21
- Docker with Compose (required for PostgreSQL and integration tests)

## Build and test

Windows: `mvnw.cmd verify`. Unix: `./mvnw verify` (or `sh mvnw verify` if the executable bit is not
preserved). Tests use real PostgreSQL through Testcontainers; H2 is not used. Flyway is the only schema
authority and Hibernate runs with `ddl-auto=validate`.

The suite validates the core business behavior at several levels:

- Domain tests cover UTC interval calculation, overlap rules, back-to-back boundaries, and deterministic ordering.
- Repository tests cover dealership/activity filtering, technician qualification, and technician/bay availability.
- PostgreSQL integration tests inspect Flyway constraints and prove overlap rejection and complete rollback.
- API tests cover create, retrieve, list, validation, not-found and conflict ProblemDetail responses.
- Idempotency tests prove repeated delivery creates one row and changed content with the same key is rejected.
- A synchronized two-request HTTP test proves that competing requests produce exactly one confirmed booking.

CI also builds the container, runs the Compose verification scenario, and performs the configured dependency audit.

## Postman and Newman API demo

The Maven/JUnit/Testcontainers suite above is the authoritative core-business test suite. The Postman collection
is supplementary: it serves as an executable API contract, a convenient video demonstration, and an optional
end-to-end regression suite for Newman. It does not replace the domain, database-constraint, rollback, or
concurrency tests that run through `mvnw verify`.

Artifacts:

- [Postman collection](postman/Unified-Service-Scheduler.postman_collection.json)
- [Local Docker Compose environment](postman/Unified-Service-Scheduler.local.postman_environment.json)
- [Detailed Postman/Newman instructions](postman/README.md)

To use Postman, import the collection and environment, select **Unified Service Scheduler — Local Docker
Compose**, and run the collection once in its defined order. It initializes unique future time windows, chains
created identifiers, asserts success and ProblemDetail response bodies, verifies back-to-back and overlap rules,
and includes a parallel race harness that requires exactly one `201` and one `409`.

To run the complete API regression collection from the command line:

```bash
newman run postman/Unified-Service-Scheduler.postman_collection.json \
  -e postman/Unified-Service-Scheduler.local.postman_environment.json \
  --iteration-count 1 --reporters cli,junit \
  --reporter-junit-export postman/newman-results.xml
```

To run only the concurrency demonstration:

```bash
newman run postman/Unified-Service-Scheduler.postman_collection.json \
  -e postman/Unified-Service-Scheduler.local.postman_environment.json \
  --folder "Concurrency Demo" --iteration-count 1
```

Newman iterations are sequential; the collection's race harness creates concurrency internally with two
asynchronous requests. Use a clean default development seed for that demo. The optional expanded demo-data
script adds alternative resources, which can legitimately allow both requests to succeed on different pairs.

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

## Load demo data manually

Docker Compose normally enables the small development Flyway seed. To populate every table with a
larger, deterministic demo dataset, run the standalone idempotent script after the containers are healthy:

```bash
docker compose exec -T postgres psql -U scheduler -d scheduler < scripts/seed-demo-data.sql
```

The script is opt-in rather than a production migration. It is safe to rerun, retains existing rows,
and prints a row-count summary. It creates two dealerships, three customers and vehicles, multiple
qualifications/services/resources, three future confirmed appointments, one cancelled appointment,
and their demo idempotency rows. If `POSTGRES_USER` or `POSTGRES_DB` was overridden, use those values
in the `psql` command.

## AI Collaboration Narrative

GenAI means **generative artificial intelligence**: a model that can produce and revise artifacts such as
requirements, designs, code, tests, and documentation from instructions and repository context. I used a GenAI
coding agent as a directed engineering collaborator, but **Spec Kit was the method I used to control that
collaboration** rather than relying on one broad prompt or accepting generated code blindly.

The Spec Kit workflow created a traceable path from the assessment to implementation:

1. The [constitution](.specify/memory/constitution.md) established non-negotiable principles: atomic booking,
   database-backed concurrency safety, real PostgreSQL tests, simple modular boundaries, privacy-safe failures,
   and human ownership of AI output.
2. The [feature specification](specs/001-service-scheduling/spec.md) translated Scenario A into testable user
   stories, edge cases, functional requirements, assumptions, and success criteria without prematurely choosing
   technology.
3. [Research](specs/001-service-scheduling/research.md), the [implementation plan](specs/001-service-scheduling/plan.md),
   [data model](specs/001-service-scheduling/data-model.md), and [OpenAPI contract](specs/001-service-scheduling/contracts/openapi.yaml)
   captured the technical decisions and their alternatives.
4. The [dependency-ordered tasks](specs/001-service-scheduling/tasks.md) turned the plan into reviewable increments,
   with business and integration tests scheduled alongside the implementation.
5. Consistency analysis and final verification compared the specification, plan, tasks, migrations, code, tests,
   and runtime behavior before acceptance.

My prompting strategy supplied explicit invariants rather than asking for a generic scheduler: UTC `Instant`
values, half-open `[start,end)` intervals, qualification and dealership eligibility, atomic appointment plus
idempotency persistence, and zero overlapping confirmed allocations. For concurrency, I reviewed the proposed
alternatives and chose PostgreSQL partial GiST exclusion constraints. I rejected application-only check-then-save
because of its race window, and rejected coarse pessimistic locking because it complicates ordering and reduces
concurrency. I also considered a transactional outbox to demonstrate Kafka/Debezium experience, but rejected it
because this monolith has no external event consumer; adding CDC infrastructure would not satisfy an actual
Scenario A requirement.

I verified AI output rather than treating generated code or generated tests as evidence. Verification included:

- inspecting the actual `REQUIRES_NEW` proxy boundary and both Flyway exclusion constraints;
- running unit, API, and Testcontainers tests against PostgreSQL rather than H2;
- synchronizing two real HTTP requests and asserting one `201`, one `409`, one retained row, and no SQL overlap;
- checking failure paths leave neither an appointment nor an orphaned idempotency record;
- reviewing generated assertions for meaningful resource IDs, intervals, status codes, and database state;
- running Compose health, live API, persistence-restart, OpenAPI, secret/PII, and Scenario A scope checks.

I refined generated work whenever implementation and claims diverged. Examples include adding composite foreign
keys for vehicle ownership and resource/dealership integrity, clarifying that candidate availability reads are
advisory while database constraints are authoritative, making idempotent losing-request recovery occur after
rollback, and documenting observability limitations rather than claiming unwired booking logs were emitted.
Final quality comes from this combination of constrained prompting, source inspection, real PostgreSQL tests,
live Compose verification, and documented limitations—not from AI generation alone.

Detailed evidence is available in [the AI collaboration narrative](docs/ai-collaboration-narrative.md),
[the verification review](docs/verification-review.md), and [the system design](docs/system-design.md).

## Scope and limitations

Authentication, UI, shifts, opening hours, cancellation actions, rescheduling, notifications, payments,
messaging, and cloud deployment are out of scope. See `docs/system-design.md` and
`docs/concurrency-decision.md`.
