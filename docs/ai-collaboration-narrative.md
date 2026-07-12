# AI Collaboration Narrative

AI was directed through constitution, specification, planning, task-generation, repeated consistency
analysis, and implementation prompts. It produced initial artifacts, Java code, migrations, tests,
container configuration, and documentation.

Human ownership requires reviewing every change and assertion. The design was repeatedly corrected:
test ordering was moved before business implementation; vehicle ownership gained a composite database
foreign key; one unambiguous transaction boundary replaced competing designs; runtime 500 behavior,
correlation contracts, idempotency readback, Linux Compose verification, and no-resource conflict tests
were made explicit. Application-only concurrency protection was rejected in favor of PostgreSQL
exclusion constraints. Generated tests must be inspected for meaningful state and database assertions.

Independent verification consists of Flyway schema inspection, direct overlap tests, back-to-back tests,
and a synchronized two-request Testcontainers race followed by a PostgreSQL row count. Defects found
during compilation and verification are recorded in `docs/verification-review.md`. Final acceptance,
security review, and understanding remain the reviewer's responsibility.
