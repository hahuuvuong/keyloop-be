# System Design

The scheduler is a Java 21/Spring Boot 3.5 modular monolith organized by scheduling feature. API
records validate and map HTTP data; application services coordinate use cases; domain values express
interval rules; infrastructure owns JPA and PostgreSQL integration. Entities never cross the API boundary.

PostgreSQL was selected for native temporal exclusion constraints. Flyway owns all DDL, Hibernate only
validates it, and Testcontainers supplies the same database technology in integration tests. Spring MVC
keeps the assessment conventional and focused; no messaging, caching, reactive stack, or distributed
architecture is required.

The public surface creates, retrieves, and lists appointments. Creation is authoritative: preliminary
availability queries improve selection, while PostgreSQL protects correctness under races and multiple
application instances.
