# Implementation Plan: Unified Service Scheduler

**Branch**: `001-service-scheduling` | **Date**: 2026-07-12 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/001-service-scheduling/spec.md`

## Summary

Build a Java 21/Spring Boot 3.5 modular-monolith service that atomically allocates an active,
qualified technician and active service bay for a fixed-duration dealership service appointment.
PostgreSQL `tstzrange` exclusion constraints are the final concurrency safeguard; application
availability queries provide deterministic selection and friendly conflicts. Flyway owns the schema,
Testcontainers PostgreSQL verifies transactions and races, and HTTP clients receive documented
ProblemDetail errors, correlation IDs, metrics, health probes, and OpenAPI documentation.

## Technical Context

**Language/Version**: Java 21 LTS

**Primary Dependencies**: Spring Boot 3.5.x, Web MVC, Jakarta Bean Validation, Data JPA/Hibernate,
Flyway, PostgreSQL driver, springdoc-openapi/Swagger UI, Actuator, Micrometer

**Storage**: PostgreSQL with `timestamptz`, `tstzrange`, `btree_gist`, Flyway-only DDL, and
`spring.jpa.hibernate.ddl-auto=validate`

**Testing**: JUnit 5; Mockito at application boundaries; Spring Boot Test; Testcontainers PostgreSQL;
HTTP-boundary and genuine concurrent-request integration tests; no H2

**Target Platform**: Linux-compatible JVM service and OCI container; local Docker Compose

**Project Type**: Backend HTTP service, modular monolith, package-by-feature

**Performance Goals**: Correctness under concurrent booking is primary; booking latency is measured.
Use paginated listing and indexed availability queries; establish measured baselines rather than an
unsupported throughput target for this assessment.

**Constraints**: UTC only; `[start,end)` intervals; atomic create; no partial state; cross-instance
double-book prevention; privacy-safe errors/logs; deterministic allocation; Scenario A only

**Scale/Scope**: One deployable service, one PostgreSQL database, four HTTP endpoints (availability
preview optional), nine core entities, one primary write workflow

## Constitution Check

*GATE: Passed before Phase 0 and re-checked after Phase 1 design.*

- [x] Booking rules, half-open intervals, UTC persistence, and atomic creation are explicit.
- [x] Application checks plus two PostgreSQL exclusion constraints prevent double-booking.
- [x] Test design covers required cases, real PostgreSQL transactions, and genuine concurrency.
- [x] Package-by-feature modular-monolith boundaries keep domain rules independent of HTTP/JPA.
- [x] Validation, ProblemDetail errors, associations, constraints, indexes, and retry safety are designed.
- [x] Correlation, privacy-safe structured logs, telemetry, health, readiness, and security are designed.
- [x] Documentation, reproducibility, AI review evidence, and Scenario A scope are planned.

**Post-design re-check**: PASS. `data-model.md`, `contracts/openapi.yaml`, and `quickstart.md`
concretely cover every gate. No constitutional exceptions are required.

## Architecture and Booking Transaction

`CreateAppointmentService.create()` is a non-transactional use-case facade. It delegates allocation
coordination to a non-transactional `AppointmentBookingCoordinator`, which invokes
`TransactionalBookingAttempt.attempt()` with `REQUIRES_NEW`; that attempt method is the sole booking
transaction boundary. It validates references and ownership, computes the interval, queries eligible
technicians and bays in stable identifier order, and attempts one combination. Availability predicates use
`existing.start_time < requested_end AND existing.end_time > requested_start` for blocking statuses.
The selected appointment is inserted and flushed before returning.

The database independently rejects overlap through partial GiST exclusion constraints on technician
and bay with `[)` ranges. If a concurrent transaction wins, the losing insert/flush raises a constraint
violation; infrastructure maps only the two known overlap constraints to a domain booking-race
conflict, the transaction rolls back, and the API returns HTTP 409 without exposing constraint names.
Correctness therefore holds across multiple application instances.

No retry is needed for a single final technician/bay combination. An outer, non-transactional
`AppointmentBookingCoordinator` MAY invoke a separate transactional attempt service with
`REQUIRES_NEW` for at most three total candidate combinations. After a recognized exclusion failure,
the failed transaction fully rolls back, that technician/bay pair is excluded, and only a known
remaining combination may be attempted. No candidate or exhausted attempts returns HTTP 409.
Idempotency is included because FR-001/FR-017/FR-018 require it: a unique key stores a canonical
request fingerprint and appointment link. Same key/same fingerprint replays HTTP 201 with the original
body and Location; same key/different fingerprint returns HTTP 400 with `IDEMPOTENCY_KEY_REUSED`.
Concurrent duplicate keys are serialized by uniqueness. A dedicated `IdempotencyRecoveryService`
performs losing readback with `REQUIRES_NEW` only after the failed booking transaction has rolled back.

### Concurrency Alternatives

- **Chosen — exclusion constraints**: directly expresses the temporal invariant, protects all writers
  and instances, permits non-overlapping concurrency, and remains authoritative if application checks race.
- **Pessimistic locking**: locking existing appointments cannot lock the absence of an overlap without
  coarse resource locks, reducing concurrency and making selection/deadlock behavior more complex.
- **Serializable transactions**: can be correct with mandatory retry handling, but raises broader
  serialization failures and expresses the scheduling invariant less directly.

## Project Structure

### Documentation (this feature)

```text
specs/001-service-scheduling/
|-- plan.md
|-- research.md
|-- data-model.md
|-- quickstart.md
|-- contracts/openapi.yaml
`-- tasks.md                 # generated by speckit-tasks
```

### Source Code (repository root)

```text
src/main/java/com/keyloop/scheduler/
|-- appointment/{api,application,domain,infrastructure}/
|-- customer/{domain,infrastructure}/
|-- vehicle/{domain,infrastructure}/
|-- dealership/{domain,infrastructure}/
|-- servicetype/{domain,infrastructure}/
|-- technician/{domain,infrastructure}/
|-- servicebay/{domain,infrastructure}/
`-- shared/{configuration,error,observability}/
src/main/resources/
|-- application.yml
`-- db/migration/
src/test/java/com/keyloop/scheduler/
|-- unit/
|-- integration/
`-- api/
docker-compose.yml
Dockerfile
.github/workflows/ci.yml
docs/{system-design.md,architecture.md,ai-collaboration-narrative.md,demo.md}
```

**Structure Decision**: One Maven module and deployable Spring Boot application. Feature packages own
their domain and persistence adapters. Appointment API records are manually mapped; JPA entities never
cross the API boundary. No generic repositories or distributed components are introduced.

## Implementation Phases and Milestones

1. **Foundation**: Java 21, Spring Boot, Maven Wrapper, package skeleton, configuration, test split, CI skeleton.
2. **Database foundation**: Compose PostgreSQL, Flyway production schema, `btree_gist`, JPA validation.
3. **Reference data**: explicit development-only seed mechanism/profile, deterministic identifiers/order.
4. **Milestone 1 vertical slice**: create and retrieve one appointment; Testcontainers proves PostgreSQL,
   Flyway, application startup, persistence, HTTP 201/Location, and GET retrieval. Later retrieval work
   hardens missing-record behavior and optimized association loading without postponing this milestone.
5. **Eligibility**: ownership, qualification, dealership, activity, interval, back-to-back rules.
6. **Concurrency integrity**: partial GiST exclusion constraints and direct database integration tests.
7. **Errors**: validation/not-found/conflict/race/operational ProblemDetail translation and rollback tests.
8. **Concurrency proof**: synchronized HTTP race against one technician and bay; exactly one row/success.
9. **Reads and docs**: listing/filtering/pagination/sorting, advisory availability preview, OpenAPI examples.
10. **Observability**: correlation filter/MDC, structured logs, counters, timer, info/health/probes/metrics.
11. **Delivery automation**: multi-stage non-root Dockerfile, healthy Compose dependencies/volume, GitHub Actions.
12. **Reviewer handoff**: README, system design/diagrams, decisions, limitations, demo, AI narrative.

## Requirement Implementation and Verification Map

| Requirements | Implementation approach | Verification |
|---|---|---|
| FR-001–FR-003 | Validated POST DTO; reference lookup; vehicle-owner rule; idempotency header | API 400/404/409 and unit tests |
| FR-004–FR-006 | Service type duration/qualification; domain interval value; UTC `Instant` to `timestamptz` | Domain calculation and migration/JPA tests |
| FR-007–FR-012 | Indexed eligibility queries; qualification join; blocking-status overlap predicate; stable ID order | Repository integration and domain tests, including inactive/back-to-back |
| FR-013–FR-016 | Transactional service, insert flush, two exclusion constraints, rollback/error adapter | PostgreSQL overlap, race, rollback, and HTTP 409 tests |
| FR-017–FR-018 | Unique idempotency key plus canonical request fingerprint | sequential and concurrent replay API tests |
| FR-019 | GET-by-ID DTO projection/manual mapping | persisted retrieval API test |
| FR-020 | Pageable GET with dealership/start range/status allowlisted sorting | filter, empty result, invalid range, pagination tests |
| FR-021 | Central ProblemDetail advice, stable codes, correlation filter, sanitization | schema/field assertions and log review |
| SC-001–SC-006 | CI test suites and reviewer quickstart/demo | Maven verify plus documented fresh-start exercise |

## Delivery and Operations

Actuator exposes health, liveness, readiness, info, and metrics; Prometheus format is optional.
Micrometer records `booking.attempts`, `booking.confirmations`, `booking.conflicts`, `booking.failures`,
and `booking.duration`. An inbound `X-Correlation-ID` is validated or generated, returned in responses,
placed in logging context, and included in ProblemDetail. Logs contain resource/appointment identifiers,
outcome, and duration—not customer personal data. Expected conflicts log at informational/warning level;
unexpected failures log as errors.

Docker uses pinned major images, multi-stage Maven build, minimal non-root runtime, and health checks.
Compose supplies local-only credentials through environment/config, waits for PostgreSQL health, retains
a named volume, and supports one-command startup. CI sets up Java 21, runs `./mvnw verify` with
Testcontainers and Flyway/JPA validation, builds the image, and executes the platform-neutral Compose
verification scenario on Linux.

## Complexity Tracking

No constitution violations. PostgreSQL-specific range/exclusion features are intentional domain
constraints, not architectural expansion.
