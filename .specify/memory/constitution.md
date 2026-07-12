<!--
Sync Impact Report
- Version change: template (unratified) -> 1.0.0
- Modified principles: Placeholder principles -> nine Unified Service Scheduler principles
- Added sections: Delivery Standards; Development and Review Workflow
- Removed sections: None (placeholder sections were concretized)
- Templates: ✅ plan-template.md; ✅ spec-template.md; ✅ tasks-template.md
- Follow-up TODOs: None
-->
# Unified Service Scheduler Constitution

## Core Principles

### I. Correctness Before Convenience
Core booking rules MUST be explicit and consistently implemented. Appointment creation MUST never
double-book a technician or service bay. Availability checking and appointment creation MUST occur
as one atomic operation. Time intervals MUST use half-open semantics, `[start, end)`, and persisted
timestamps MUST use UTC. Business invariants MUST be protected at both application and database
levels where practical.

Rationale: scheduling correctness requires unambiguous boundaries and concurrency-safe enforcement.

### II. Test-First Business Logic
Core business behavior MUST be developed with automated tests. Every user story MUST have
independently verifiable acceptance scenarios. Tests MUST cover successful bookings, unavailable
resources, qualification matching, overlapping appointments, back-to-back appointments, invalid
requests, and concurrent booking attempts. Integration tests MUST verify persistence and
transactional behavior using the real database technology selected in the implementation plan. A
feature is not complete until its acceptance tests pass.

Rationale: executable examples protect the transaction and concurrency behavior most likely to fail.

### III. Simple, Maintainable Architecture
The design MUST prefer a modular monolith with clear domain, application, API, and infrastructure
boundaries. Unnecessary abstractions, generic repositories, microservices, messaging systems, and
distributed infrastructure MUST be avoided unless a concrete requirement justifies them. Business
rules MUST remain independent of HTTP and persistence concerns. Names MUST reflect the scheduling
domain. Public API behavior and significant architectural decisions MUST be documented.

Rationale: focused boundaries preserve clarity without unsupported operational or conceptual cost.

### IV. API and Data Integrity
API inputs MUST be validated at the system boundary. Failures MUST use consistent, machine-readable
error responses. Confirmed appointments MUST persist associations to the customer, vehicle,
dealership, service type, technician, and service bay. Database relationships, constraints, and
indexes MUST support documented domain invariants. Retried operations SHOULD support safe
duplicate-request handling; any omission MUST be justified in the implementation plan.

Rationale: explicit contracts and relational safeguards prevent partial or ambiguous bookings.

### V. Observability and Operability
Requests MUST have correlation identifiers. Logs MUST be structured and MUST NOT expose sensitive
customer data. Booking attempts, successes, conflicts, failures, and latency SHOULD be measurable.
Database and booking operations SHOULD be traceable. The application MUST provide health and
readiness checks. Operational failures MUST be distinguishable from expected booking conflicts.

Rationale: transaction behavior must be diagnosable without compromising privacy.

### VI. Security and Privacy
The system MUST collect and expose only customer and vehicle data required by the scenario. Secrets
MUST come from configuration and MUST never be committed. Logs and error responses MUST avoid
personal information and internal implementation details. Dependencies and input handling MUST
follow secure defaults.

Rationale: data minimization and safe defaults reduce risk while keeping the assessment focused.

### VII. Specification Discipline
Feature specifications MUST describe WHAT users need and WHY without selecting frameworks,
databases, libraries, deployment tools, architecture patterns, or implementation techniques.
Technical choices belong exclusively in the implementation plan. Ambiguities MUST be explicit
assumptions or resolved through clarification. Requirements MUST be testable and traceable to
acceptance scenarios and tasks. Scope additions MUST be classified as required, optional, or future.

Rationale: separating requirements from solutions preserves unbiased planning and traceability.

### VIII. AI-Assisted Engineering with Human Ownership
AI MAY assist with design, implementation, tests, documentation, and review. AI-generated output
MUST be inspected, tested, and understood before acceptance. Database concurrency, transaction
boundaries, security, and business rules require deliberate human verification. Important prompts,
corrections, rejected suggestions, verification steps, and lessons learned MUST be recorded for the
AI Collaboration Narrative. Passing generated tests alone is insufficient; tests MUST be reviewed
for meaningful assertions.

Rationale: AI can accelerate delivery, but accountability and evidence remain human responsibilities.

### IX. Delivery Quality
The repository MUST include reproducible build, run, migration, seed, and test instructions. A fresh
reviewer MUST be able to start the system and exercise the main booking scenario. The final
implementation MUST include automated tests, API documentation, documented assumptions, known
limitations, and architectural decisions. Changes MUST remain focused on selected Scenario A
requirements.

Rationale: assessment behavior and decisions must be reproducible and independently evaluated.

## Delivery Standards

- Scheduling contracts MUST use UTC timestamps and document `[start, end)` semantics.
- Persistence design MUST identify constraints, indexes, and transaction boundaries protecting
  technician and bay availability under concurrency.
- API documentation MUST define validation, machine-readable errors, conflicts, correlation
  identifiers, and duplicate-request handling where applicable.
- Operational documentation MUST cover configuration, secrets, health, readiness, logging, metrics,
  and tracing without exposing sensitive data.
- Required, optional, and future scope MUST remain classified; implementation is limited to Scenario A.

## Development and Review Workflow

1. Specifications MUST remain technology-agnostic and link requirements to acceptance scenarios.
2. Plans MUST justify technical choices, define atomic booking transactions and database protections,
   and include a Constitution Check before research and after design.
3. Tasks MUST preserve traceability and schedule tests before implementation, including real-database
   integration and concurrent-booking tests.
4. Reviews MUST verify meaningful assertions, boundaries, validation, privacy, operability,
   reproducibility, and Scenario A scope.
5. Constitutional violations MUST be justified before implementation in Complexity Tracking;
   unjustified violations block implementation.
6. Completion requires passing acceptance tests and human review of business rules, concurrency,
   transaction boundaries, security, and AI-generated work.

## Governance

This constitution governs all specifications, plans, tasks, implementation, and reviews and
supersedes conflicting practices. Each plan MUST include a Constitution Check, repeated after
design. Compliance MUST be reviewed before implementation and before feature completion. Any
violation MUST be explicitly justified and documented before implementation.

Amendments MUST document the reason, affected principles or sections, migration impact, version
change, and amendment date. They take effect only after dependent templates are synchronized.
Versions follow semantic versioning: MAJOR for incompatible governance changes or principle
removals/redefinitions, MINOR for new principles or materially expanded obligations, and PATCH for
non-semantic clarifications. Reviewers MUST reject work without demonstrated compliance or an
approved, documented exception.

**Version**: 1.0.0 | **Ratified**: 2026-07-12 | **Last Amended**: 2026-07-12
