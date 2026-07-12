# Tasks: Unified Service Scheduler

**Input**: Design artifacts in `specs/001-service-scheduling/`

**Tests**: Required and written before corresponding implementation. PostgreSQL behavior MUST use
Testcontainers; H2 and mocked concurrency are prohibited.

**Format**: `[ID] [P?] [Story?] Description with exact file path`

## Phase 1: Project Foundation and Automated Build

**Purpose**: Establish the reproducible Java 21 service and build before domain work.

- [x] T001 Create Java 21 Spring Boot 3.5.x Maven project and package-by-feature directories in `pom.xml` and `src/main/java/com/keyloop/scheduler/`
- [x] T002 Add and verify Maven Wrapper scripts and pinned wrapper configuration in `mvnw`, `mvnw.cmd`, and `.mvn/wrapper/maven-wrapper.properties`
- [x] T003 Configure Web MVC, Validation, Data JPA, PostgreSQL, Flyway, springdoc, Actuator, Micrometer, and test dependencies in `pom.xml`
- [x] T004 [P] Create application entry point in `src/main/java/com/keyloop/scheduler/SchedulerApplication.java`
- [x] T005 [P] Configure safe shared defaults, UTC serialization, Hibernate `ddl-auto=validate`, Flyway, and Actuator probes in `src/main/resources/application.yml`
- [x] T006 [P] Configure local environment overrides without committed secrets in `src/main/resources/application-local.yml` and `.env.example`
- [x] T007 [P] Configure test profile and Testcontainers service connection base class in `src/test/resources/application-test.yml` and `src/test/java/com/keyloop/scheduler/support/PostgresIntegrationTest.java`
- [x] T008 Add Maven unit/integration test execution, dependency vulnerability auditing, and build quality settings in `pom.xml`

**Completion criterion**: `mvnw verify` compiles the empty application on Java 21 and discovers both
unit and integration test categories with no unresolved configuration secrets.

---

## Phase 2: Database, Domain, and Shared Foundations

**Purpose**: Create the schema and common boundaries that block all user stories.

**CRITICAL**: Complete this phase before story implementation.

- [x] T009 Create PostgreSQL service, health check, persistent volume, and local-only environment wiring in `docker-compose.yml`
- [x] T010 Write failing schema tests for `btree_gist`, vehicle-owner integrity, both `[)` exclusion definitions, and blocking behavior in `src/test/java/com/keyloop/scheduler/integration/SchemaMigrationIT.java`
- [x] T011 Enable `btree_gist` and create the reference schema with vehicle ownership uniqueness in `src/main/resources/db/migration/V1__enable_btree_gist.sql` and `src/main/resources/db/migration/V2__create_reference_tables.sql`
- [x] T012 Create appointment and idempotency tables with UTC columns, composite vehicle-owner FK, status/interval checks, foreign keys, and search indexes in `src/main/resources/db/migration/V3__create_appointment_tables.sql`
- [x] T013 Create partial GiST exclusion constraints for confirmed technician and bay `[)` ranges in `src/main/resources/db/migration/V4__add_appointment_exclusion_constraints.sql`
- [x] T014 Run and complete the T010 migration assertions after V1â€“V4 so PostgreSQL schema behavior passes in `src/test/java/com/keyloop/scheduler/integration/SchemaMigrationIT.java`
- [x] T015 [P] Implement dealership, customer, vehicle, qualification, and service-type JPA entities/repositories in `src/main/java/com/keyloop/scheduler/{dealership,customer,vehicle,servicetype}/infrastructure/`
- [x] T016 [P] Implement technician, technician-qualification, and service-bay JPA entities/repositories in `src/main/java/com/keyloop/scheduler/{technician,servicebay}/infrastructure/`
- [x] T017 Implement appointment and idempotency JPA entities with explicit associations and no API exposure in `src/main/java/com/keyloop/scheduler/appointment/infrastructure/`
- [x] T018 [P] Write failing domain unit tests for end-time calculation, UTC normalization, `[)` overlap, back-to-back intervals, and status blocking in `src/test/java/com/keyloop/scheduler/unit/appointment/domain/AppointmentIntervalTest.java`
- [x] T019 Implement domain identifiers, UTC appointment interval, status, duration, and qualification concepts to pass T018 in `src/main/java/com/keyloop/scheduler/appointment/domain/`
- [x] T020 Create deterministic development-only seed migration/configuration separated from production migrations in `src/main/resources/db/dev-migration/R__development_seed.sql` and `src/main/resources/application-dev.yml`
- [x] T021 [P] Create deterministic Testcontainers fixture builder for all reference entities in `src/test/java/com/keyloop/scheduler/support/SchedulingFixtures.java`
- [x] T022 [P] Implement stable domain exceptions and error codes in `src/main/java/com/keyloop/scheduler/shared/error/`
- [x] T023 [P] Implement correlation-ID validation/generation/response propagation and logging context in `src/main/java/com/keyloop/scheduler/shared/observability/CorrelationIdFilter.java`
- [x] T024 [P] Configure structured privacy-safe logging fields and redaction conventions in `src/main/resources/logback-spring.xml`

**Completion criterion**: PostgreSQL starts, all Flyway migrations and Hibernate validation pass, the two
exclusion constraints exist, deterministic fixtures load, and foundational domain tests pass.

---

## Phase 3: User Story 1 â€” Create a Confirmed Appointment (Priority: P1) â€” MVP

**Goal**: Atomically persist one valid confirmed booking with deterministic resources and safe retry behavior.

**Independent Test**: Against Testcontainers PostgreSQL, submit a valid HTTP request, receive 201 and
Location, verify all associations/times/status in the database, then replay the request without duplication.

### Tests first

- [x] T025 [P] [US1] Write unit tests for active/dealership/qualification eligibility and deterministic candidate ordering in `src/test/java/com/keyloop/scheduler/unit/appointment/domain/ResourceEligibilityTest.java`
- [x] T026 [P] [US1] Write application tests for creation, successful retrieval with all associations, unknown-ID retrieval, ownership, interval, allocation, rollback, and idempotency in `src/test/java/com/keyloop/scheduler/unit/appointment/application/CreateAppointmentServiceTest.java`
- [x] T027 [P] [US1] Write repository integration tests for qualified technician and available bay queries, inactive/cross-dealership exclusion, overlap, and back-to-back acceptance in `src/test/java/com/keyloop/scheduler/integration/AvailabilityRepositoryIT.java`
- [x] T028 [P] [US1] Write Testcontainers HTTP tests for 201/Location, full GET retrieval from Location, and unknown-ID 404 before endpoint implementation in `src/test/java/com/keyloop/scheduler/api/CreateAppointmentApiIT.java`
- [x] T029 [P] [US1] Write ten-replay and concurrent same-key tests asserting one row, repeated 201/original Location/body, plus changed-payload 400 in `src/test/java/com/keyloop/scheduler/api/AppointmentIdempotencyIT.java`

### Implementation

- [x] T030 [US1] Implement request command, result model, ports, canonical fingerprinting, and idempotency rules in `src/main/java/com/keyloop/scheduler/appointment/application/`
- [x] T031 [US1] Implement ownership/reference loaders and deterministic qualified-technician/available-bay queries in `src/main/java/com/keyloop/scheduler/appointment/infrastructure/AppointmentAllocationRepository.java`
- [x] T032 [US1] Implement non-transactional creation facade/coordinator, sole `REQUIRES_NEW` booking-attempt transaction, and milestone retrieval service in `src/main/java/com/keyloop/scheduler/appointment/application/CreateAppointmentService.java`, `src/main/java/com/keyloop/scheduler/appointment/application/AppointmentBookingCoordinator.java`, `src/main/java/com/keyloop/scheduler/appointment/application/TransactionalBookingAttempt.java`, and `src/main/java/com/keyloop/scheduler/appointment/application/GetAppointmentService.java`
- [x] T033 [US1] Implement idempotency persistence and dedicated post-rollback `REQUIRES_NEW` readback in `src/main/java/com/keyloop/scheduler/appointment/infrastructure/JpaIdempotencyRepository.java` and `src/main/java/com/keyloop/scheduler/appointment/application/IdempotencyRecoveryService.java`
- [x] T034 [P] [US1] Create validated Java record request/response/resource DTOs and manual mapper in `src/main/java/com/keyloop/scheduler/appointment/api/`
- [x] T035 [US1] Implement POST creation and minimal GET-by-ID from Location, including repeated 201 replay semantics, in `src/main/java/com/keyloop/scheduler/appointment/api/AppointmentController.java`
- [x] T036 [US1] Add booking attempt/confirmation/conflict/failure counters and duration timer around creation in `src/main/java/com/keyloop/scheduler/appointment/application/BookingMetrics.java`

**Completion criterion**: US1 tests T025â€“T029 pass; exactly one confirmed appointment is persisted and
retrievable by its Location with all associations, deterministic resources, UTC `[)` times, and retry safety.

---

## Phase 4: User Story 2 â€” Reject Unavailable or Invalid Appointments (Priority: P1)

**Goal**: Prevent every partial, invalid, unavailable, overlapping, or race-losing booking and return a safe outcome.

**Independent Test**: Exercise invalid references, no resources, direct overlaps, and simultaneous HTTP
requests; verify 400/404/409 outcomes, rollback, exactly one race winner, and no database overlap.

### Tests first

- [x] T037 [P] [US2] Write HTTP tests for malformed input, inconsistent vehicle owner, and unknown references returning distinct 400/404 ProblemDetails in `src/test/java/com/keyloop/scheduler/api/AppointmentValidationApiIT.java`
- [x] T038 [P] [US2] Write integration tests proving both exclusion constraints reject overlap, allow back-to-back rows, and ignore cancelled rows in `src/test/java/com/keyloop/scheduler/integration/AppointmentExclusionConstraintIT.java`
- [x] T039 [P] [US2] Write transaction integration tests proving allocation/persistence failures leave no appointment or idempotency residue in `src/test/java/com/keyloop/scheduler/integration/AppointmentRollbackIT.java`
- [x] T040 [P] [US2] Write ProblemDetail tests for 400/404, no-qualified-technician-or-bay HTTP 409 `BOOKING_CONFLICT` with no appointment/idempotency residue, and a forced unexpected dependency failure returning sanitized 500 with code/title/detail/path/timestamp/correlation ID and no SQL, constraint names, stack traces, or PII in `src/test/java/com/keyloop/scheduler/api/ProblemDetailApiIT.java`
- [x] T041 [US2] Write synchronized Testcontainers HTTP race test with one technician/bay, two overlapping requests, at most one 201, loser 409, and exactly one database row in `src/test/java/com/keyloop/scheduler/api/ConcurrentBookingApiIT.java`

### Implementation

- [x] T042 [US2] Translate only known technician/bay exclusion violations into booking-race domain conflicts and rethrow unknown database failures in `src/main/java/com/keyloop/scheduler/appointment/infrastructure/BookingConstraintTranslator.java`
- [x] T043 [US2] Extend the existing non-transactional coordinator to exclude failed pairs, invoke at most three `REQUIRES_NEW` attempts, and return final 409 only after each failed attempt fully rolls back in `src/main/java/com/keyloop/scheduler/appointment/application/AppointmentBookingCoordinator.java`
- [x] T044 [US2] Implement centralized Spring ProblemDetail advice for 400/404/409/500 with stable safe codes in `src/main/java/com/keyloop/scheduler/shared/error/GlobalExceptionHandler.java`
- [x] T045 [US2] Add outcome-specific privacy-safe structured logging with correlation IDs in `src/main/java/com/keyloop/scheduler/appointment/application/BookingAuditLogger.java`

**Completion criterion**: US2 tests T037â€“T041 pass; expected conflicts are distinguishable from failures,
all failed transactions are clean, and repeated concurrency runs prove at most one confirmed overlap winner.

---

## Phase 5: User Story 3 â€” Retrieve a Confirmed Appointment (Priority: P2)

**Goal**: Retrieve every required detail of a persisted appointment without exposing JPA entities.

**Independent Test**: Retrieve the US1-created appointment and verify all fields; retrieve an unknown UUID and receive 404.

### Tests first

- [x] T046 [P] [US3] Write application/repository retrieval tests for complete associations and missing identifiers in `src/test/java/com/keyloop/scheduler/integration/GetAppointmentIT.java`
- [x] T047 [P] [US3] Write HTTP tests for successful appointment representation, URI references, and safe 404 ProblemDetail in `src/test/java/com/keyloop/scheduler/api/GetAppointmentApiIT.java`

### Implementation

- [x] T048 [US3] Harden milestone retrieval with complete association fetch, optimized query, and missing-record behavior in `src/main/java/com/keyloop/scheduler/appointment/application/GetAppointmentService.java` and `src/main/java/com/keyloop/scheduler/appointment/infrastructure/AppointmentQueryRepository.java`
- [x] T049 [US3] Implement GET `/api/v1/appointments/{appointmentId}` using manual DTO mapping in `src/main/java/com/keyloop/scheduler/appointment/api/AppointmentController.java`

**Completion criterion**: US3 tests T046â€“T047 pass and retrieval returns all required persisted fields or a safe 404.

---

## Phase 6: User Story 4 â€” List and Filter Appointments (Priority: P3)

**Goal**: Return deterministic paginated appointment lists with optional documented filters.

**Independent Test**: Seed appointments across dealerships, dates, and statuses and verify unfiltered,
filtered, empty, invalid-range, paginated, and allowlisted-sort results.

### Tests first

- [x] T050 [P] [US4] Write repository integration tests for dealership, half-open start-date range, status, deterministic sorting, and pagination in `src/test/java/com/keyloop/scheduler/integration/ListAppointmentsIT.java`
- [x] T051 [P] [US4] Write HTTP tests for combined filters, empty pages, invalid/reversed ranges, page limits, and rejected sort fields in `src/test/java/com/keyloop/scheduler/api/ListAppointmentsApiIT.java`

### Implementation

- [x] T052 [US4] Implement list criteria, range validation, allowlisted sorting, and pageable query service in `src/main/java/com/keyloop/scheduler/appointment/application/ListAppointmentsService.java`
- [x] T053 [US4] Implement filtered GET `/api/v1/appointments` and page DTO mapping in `src/main/java/com/keyloop/scheduler/appointment/api/AppointmentController.java`

**Completion criterion**: US4 tests T050â€“T051 pass and lists return only deterministic matches with bounded pagination.

---

## Phase 7: API Documentation and Optional Advisory Preview

**Purpose**: Publish the implemented contract; preview remains optional and non-authoritative.

- [x] T054 [P] Add springdoc endpoint descriptions, correlation request/response headers, validation/status documentation, success and both conflict examples, and ProblemDetail schemas in `src/main/java/com/keyloop/scheduler/appointment/api/AppointmentOpenApi.java`
- [x] T055 Synchronize generated behavior with `specs/001-service-scheduling/contracts/openapi.yaml` and test applicable 400/404/409/500 responses plus replay semantics in `src/test/java/com/keyloop/scheduler/api/OpenApiContractIT.java`
- [x] T056 [P] Implement advisory availability preview only if time permits, clearly marked non-authoritative, in `src/main/java/com/keyloop/scheduler/appointment/api/AvailabilityController.java`
- [x] T057 [P] If T056 is implemented, test preview eligibility and creation-authority race behavior in `src/test/java/com/keyloop/scheduler/api/AvailabilityPreviewApiIT.java`

**Completion criterion**: Required endpoints and errors appear in Swagger UI with accurate examples and schemas;
if preview is omitted, the optional path is removed from the delivered contract.

---

## Phase 8: Operability, Containers, and Continuous Integration

**Purpose**: Make the service observable, reproducible, and continuously verified.

- [x] T058 [P] Configure health, liveness, readiness, info, metrics exposure, and safe endpoint access in `src/main/resources/application.yml`
- [x] T059 [P] Write observability integration tests for probes, correlation propagation, counters, timer, and conflict-versus-failure metrics in `src/test/java/com/keyloop/scheduler/integration/ObservabilityIT.java`
- [x] T060 Implement pinned multi-stage build and non-root runtime with application health check in `Dockerfile`
- [x] T061 Complete application service, health dependency, environment wiring, and one-command startup in `docker-compose.yml`
- [x] T062 [P] Create GitHub Actions Java 21 workflow running Maven/dependency verification, Testcontainers, Flyway validation, image build, and Linux Compose scenario verification in `.github/workflows/ci.yml`
- [x] T063 Validate clean Compose startup, volume behavior, app/database health, migrations, and primary scenario with a platform-neutral script in `scripts/verify-compose.sh`

**Completion criterion**: CI fails on tests or migration validation; Compose reaches readiness from a clean
checkout; metrics/logs trace bookings without customer PII; the container runs without root where practical.

---

## Phase 9: Delivery Documentation and Final Quality Gate

**Purpose**: Enable a fresh reviewer to understand, run, inspect, and demonstrate Scenario A.

- [x] T064 [P] Write build/run/test/migration/seed instructions, API examples, replay behavior, and clarify canonical resource URIs are not guaranteed dereferenceable in `README.md`
- [x] T065 [P] Write System Design Document with component responsibilities and technology justifications in `docs/system-design.md`
- [x] T066 [P] Create architecture, booking data-flow, and ER diagrams with explanatory text in `docs/architecture.md`
- [x] T067 [P] Document exclusion-constraint decision, cross-instance correctness, alternatives, error translation, and retry policy in `docs/concurrency-decision.md`
- [x] T068 [P] Document correlation, structured logging, metrics, health, readiness, and privacy strategy in `docs/observability.md`
- [x] T069 Write AI Collaboration Narrative covering direction, outputs, review, rejections, independent concurrency/database verification, assertion review, defects, lessons, and ownership in `docs/ai-collaboration-narrative.md`
- [x] T070 [P] Write step-by-step video demonstration script for startup, Swagger, success, retrieval, conflict, back-to-back, listing, metrics, and concurrency proof in `docs/video-demo-script.md`
- [x] T071 Execute and record the fresh-reviewer quickstart and demonstration evidence in `docs/demo-results.md`
- [x] T072 Review every automated test for meaningful assertions and record human verification of business rules, transactions, security, and concurrency in `docs/verification-review.md`
- [x] T073 Run final Maven verification and dependency audit, Compose validation, OpenAPI review, secret/PII scan, and Scenario A scope audit; record results in `docs/verification-review.md`

**Completion criterion**: A fresh reviewer reproduces the scenario from documented commands, all tests and
quality gates pass, documentation is complete, and no out-of-scope feature or secret is included.

---

## Dependencies and Execution Order

```text
Phase 1 Setup
  -> Phase 2 Database/Domain Foundations
      -> US1 Create (MVP)
          -> US2 Conflict/Concurrency
          -> US3 Retrieve
          -> US4 List
              -> API Docs / Optional Preview
                  -> Operability / Containers / CI
                      -> Documentation / Final Gate
```

- US2 depends on US1's transactional creation path and schema constraints.
- US3 depends on the persisted US1 appointment but has an independent read endpoint and test criterion.
- US4 depends on the appointment schema/mapper but not on US3 behavior.
- After US1, US3 and initial US4 query work can proceed in parallel with US2 in different files; shared
  controller edits require coordination.

## Parallel Execution Examples

- **Foundation**: T004â€“T007 and T015/T016/T021â€“T024 target independent files after migrations;
  T018 MUST complete before T019.
- **US1**: T025â€“T029 tests can be authored in parallel; T034 DTO work can proceed alongside T030â€“T033.
- **US2**: T037â€“T040 can be authored in parallel; T041 follows usable POST behavior and constraints.
- **US3**: T046 and T047 can be authored in parallel before T048â€“T049.
- **US4**: T050 and T051 can be authored in parallel before T052â€“T053.
- **Delivery**: T064â€“T070 can proceed in parallel once behavior stabilizes.

## Requirement Coverage

| Requirement | Implementation tasks | Automated verification tasks | Completion gate |
|---|---|---|---|
| FR-001â€“FR-003 | T030â€“T035 | T026, T028, T037 | US1/US2 |
| FR-004â€“FR-006 | T011, T012, T019, T032 | T014, T018, T026, T028 | Foundation/US1 |
| FR-007â€“FR-012 | T015, T016, T031, T032 | T025â€“T028, T038 | US1/US2 |
| FR-013â€“FR-016 | T013, T032, T042, T043 | T038, T039, T041 | US2 |
| FR-017â€“FR-018 | T012, T017, T030, T033, T035 | T029 | US1 |
| FR-019 | T032, T035, T048, T049 | T026, T028, T046, T047 | US1 milestone/US3 hardening |
| FR-020 | T052, T053 | T050, T051 | US4 |
| FR-021 | T022â€“T024, T042, T044, T045 | T037, T040, T059 | US2/Operations |

## Implementation Strategy

The MVP is Phases 1â€“3: PostgreSQL starts, Flyway runs, Spring starts, one appointment is atomically
created and retrieved through its Location, and Testcontainers proves persistence. Phase 4 makes the MVP concurrency-safe
and error-complete before read features expand. Each later phase preserves all earlier acceptance tests.

Do not implement authentication, UI, shifts, opening hours, notifications, rescheduling, cancellation API,
payments, messaging, Redis, microservices, Kubernetes, cloud deployment, or full monitoring infrastructure.

