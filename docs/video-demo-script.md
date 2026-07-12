# Video Demonstration Script

1. Show Java 21 and run `./mvnw verify` with Testcontainers PostgreSQL.
2. Run `docker compose up --build`; show readiness and Swagger UI.
3. Create a booking and show 201, Location, resources, UTC start/end, and Confirmed status.
4. GET Location and list using dealership/date/status/page filters.
5. Replay the key and show one row; change payload and show safe 400.
6. Show overlapping 409 and successful back-to-back booking.
7. Run the synchronized concurrency test and show exactly one database row.
8. Show correlation ID, structured logs, health probes, and booking metrics.
