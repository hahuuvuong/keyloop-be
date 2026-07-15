# AI Collaboration Narrative

## Strategy: specification-driven AI collaboration

GenAI means **generative artificial intelligence**: software that can generate and revise requirements, designs,
code, tests, and documentation from natural-language instructions and repository context. I used a GenAI coding
agent throughout this assessment, but I did not treat it as an autonomous source of truth. I used **Spec Kit as
the engineering control framework** for the collaboration, with reviewable artifacts and human decisions between
each stage.

My high-level strategy was:

```text
Assessment brief
    -> Constitution
    -> Feature specification and assumptions
    -> Research and implementation plan
    -> Data model and API contract
    -> Dependency-ordered tasks
    -> AI-assisted implementation
    -> Cross-artifact analysis and refinement
    -> Real PostgreSQL verification and human acceptance
```

This was deliberately different from asking an agent to "build an appointment scheduler" in one prompt. Each
stage constrained the next one, preserved traceability to Scenario A, and gave me a checkpoint where I could
reject or correct generated output before it reached the codebase.

## How I guided the agent with Spec Kit

I began with the [project constitution](../.specify/memory/constitution.md). It made the non-negotiable principles
explicit before technology selection: no technician or bay double-booking, one atomic booking operation, UTC
timestamps, half-open `[start,end)` intervals, real-database tests, machine-readable privacy-safe errors, simple
modular-monolith boundaries, and human verification of AI-generated work.

I then used the Spec Kit lifecycle to produce and review:

- the [feature specification](../specs/001-service-scheduling/spec.md), containing user stories, acceptance
  scenarios, edge cases, assumptions, functional requirements, and measurable outcomes;
- [research decisions](../specs/001-service-scheduling/research.md), where concurrency, time representation,
  idempotency, testing, and scope alternatives were compared;
- the [implementation plan](../specs/001-service-scheduling/plan.md), which fixed the modular architecture,
  transaction boundary, technology choices, and requirement-to-verification map;
- the [data model](../specs/001-service-scheduling/data-model.md) and
  [OpenAPI contract](../specs/001-service-scheduling/contracts/openapi.yaml);
- [dependency-ordered tasks](../specs/001-service-scheduling/tasks.md), which kept tests and migrations tied to
  the acceptance scenarios rather than postponing verification until the end;
- consistency and completion reviews comparing specification, plan, tasks, source code, migrations, tests, and
  observed behavior.

My prompts emphasized invariants and evidence. Examples included requiring PostgreSQL rather than H2 for overlap
tests, checking database row counts after failures, synchronizing concurrent HTTP requests rather than simulating
concurrency with sequential calls, and requiring documentation to describe the transaction boundary actually
present in the code.

## Decisions I owned and suggestions I rejected

The central decision was to use two PostgreSQL partial GiST exclusion constraints for confirmed technician and
service-bay intervals. AI helped compare application-only checks, pessimistic locks, optimistic locking, and
serializable transactions, but I selected and verified the database constraint because it directly protects the
invariant across threads and application instances. The availability query remains useful for selection, while
the database is authoritative at flush.

I also pushed back on unnecessary architecture. I initially considered adding a transactional outbox to showcase
Kafka and Debezium experience. I rejected it because this modular monolith has no external event consumer and
already commits the appointment and idempotency record atomically in PostgreSQL. An outbox would have added a
table, CDC or relay operations, consumer idempotency, and new failure modes without satisfying a Scenario A
requirement. It remains a conditional future option if notifications, a technician application, or another real
consumer is introduced.

Other refinements made during human review included:

- moving meaningful tests alongside or ahead of business implementation tasks;
- adding a composite foreign key so the persisted vehicle/customer association cannot be inconsistent;
- adding composite foreign keys so assigned technicians and bays belong to the appointment dealership;
- keeping one unambiguous `REQUIRES_NEW` booking-attempt boundary;
- recovering idempotent losing requests only after the failed write transaction has rolled back;
- distinguishing advisory availability reads from the database's authoritative concurrency guarantee;
- documenting actual error and observability behavior instead of repeating intended behavior that was not wired.

## Verification and refinement process

I did not accept generated tests merely because they passed. I inspected whether their assertions proved the
business invariant and added database-state checks where necessary. Verification included:

- unit tests for interval calculation, positive duration, overlap, back-to-back boundaries, deterministic
  ordering, and request fingerprint behavior;
- repository tests for active status, dealership ownership, qualification matching, and interval availability;
- Flyway/schema inspection proving `btree_gist`, foreign keys, indexes, and both `[)` exclusion constraints;
- direct PostgreSQL tests proving confirmed overlap is rejected, back-to-back rows are accepted, and cancelled
  rows do not block resources;
- API tests for creation, retrieval, listing, validation, not-found outcomes, conflicts, and safe ProblemDetail;
- rollback checks confirming a failed request leaves neither an appointment nor an idempotency row;
- idempotency tests proving ten repeated requests create one appointment and a changed payload is rejected;
- a synchronized two-request Testcontainers race asserting one `201`, one `409`, one retained appointment, and
  no overlapping confirmed technician or bay rows;
- Docker Compose health, live API, OpenAPI, restart persistence, secret/PII, and Scenario A scope reviews.

Defects and mismatches found during compilation, test execution, live demonstration, or documentation review were
fed back into the relevant artifact rather than patched only at the final layer. The evidence and remaining audit
limitations are recorded in the [verification review](verification-review.md).

## How I ensured final quality

Final quality did not come from the volume of generated code. It came from keeping requirements traceable,
constraining the agent with explicit invariants, reviewing every concurrency and transaction decision, using the
real database technology, checking persistent state after failures, and documenting known limitations honestly.
The GenAI agent accelerated artifact creation and comparison; I retained responsibility for scope, architectural
judgment, meaningful assertions, security/privacy review, and acceptance of the final repository.
