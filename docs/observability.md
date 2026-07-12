# Observability

Every request accepts a safe `X-Correlation-ID` or receives a generated UUID; it is returned, placed in
logging context, and included in ProblemDetail. Structured logs include outcome and resource identifiers,
never customer display data. Expected conflicts are warnings; failures are errors.

Actuator exposes health, liveness, readiness, info, metrics, and Prometheus output. Micrometer records
booking attempts, confirmations, conflicts, failures, and duration.
