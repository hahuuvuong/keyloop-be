# Research: Unified Service Scheduler

## PostgreSQL-enforced overlap prevention

**Decision**: Enable `btree_gist` and use partial GiST exclusion constraints combining resource equality
with `tstzrange(start_time, end_time, '[)') &&`, applying only to `CONFIRMED` appointments.

**Rationale**: This directly and atomically enforces the temporal invariant across transactions and
application instances while allowing back-to-back intervals.

**Alternatives considered**: Pessimistic resource locks are coarser and deadlock-prone; serializable
transactions require generalized retry handling; application-only checks race.

## Time representation

**Decision**: Use `Instant` in domain/persistence and ISO-8601 offset timestamps at the API boundary;
persist as `timestamptz`. Reject timestamps lacking an offset and normalize to UTC.

**Rationale**: `Instant` makes the stored meaning unambiguous while the API remains readable.

**Alternatives considered**: Local date-time is ambiguous; retaining arbitrary offsets adds no scheduling value.

## Idempotency

**Decision**: Include required `Idempotency-Key` for POST. Persist the key, SHA-256 canonical request
fingerprint, and appointment association under a uniqueness constraint in the booking transaction.
An identical replay returns HTTP 201 with the original representation and Location; a changed payload
returns HTTP 400 `IDEMPOTENCY_KEY_REUSED`.

**Rationale**: The specification explicitly requires safe retry behavior and differing-payload rejection.

**Alternatives considered**: Optional keys leave retries unsafe; appointment-field uniqueness would reject
valid repeat services and does not identify client intent.

## JPA and range constraints

**Decision**: Map start/end timestamps normally and create the `tstzrange` expression in Flyway exclusion
constraints rather than requiring a range-valued JPA field. Flyway is schema authority; Hibernate validates.

**Rationale**: Domain code needs instants, while PostgreSQL can construct the range in its native constraint.

**Alternatives considered**: A generated range column adds mapping/schema surface without assessment value.

## Resource selection and retry

**Decision**: Query eligible resources ordered by stable identifier and test candidate combinations in that
order. Flush the selected insert. On a recognized overlap race, normally return 409; optionally retry up to
two alternative combinations in new transactions when known alternatives remain.

**Rationale**: Determinism makes tests repeatable, while bounded retry can improve success without weakening
the database invariant or hiding sustained contention.

**Alternatives considered**: Random selection is hard to reproduce; unbounded retry harms latency; locking all
candidates reduces concurrency.

## Test and delivery strategy

**Decision**: Use pure JUnit domain tests, focused Mockito application tests, and Spring Boot/Testcontainers
PostgreSQL integration and HTTP tests. Run all through Maven Wrapper in CI and use Docker Compose for demos.

**Rationale**: Real PostgreSQL is mandatory to verify Flyway, GiST constraints, transactions, and concurrency.

**Alternatives considered**: H2 cannot faithfully represent PostgreSQL range/exclusion behavior; mocked
concurrency cannot prove database integrity.

## Scope decisions

**Decision**: Implement Confirmed and Cancelled status semantics in storage and availability rules, but no
cancellation endpoint. Availability preview is optional/advisory. Prometheus exposition and ADRs are optional.

**Rationale**: These choices support required invariants without expanding into future user journeys.

**Alternatives considered**: A cancellation workflow, UI, messaging, auth, shifts, and operating hours are deferred.
