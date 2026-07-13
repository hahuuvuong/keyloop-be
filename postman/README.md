# Unified Service Scheduler Postman suite

This suite is matched to the implemented Scenario A API under `/api/v1`, the Spring controllers, error handler, allocation logic, and the Docker Compose `dev` profile.

## Files

- `Unified-Service-Scheduler.postman_collection.json` — Postman Collection v2.1 demo and regression suite.
- `Unified-Service-Scheduler.local.postman_environment.json` — local Docker Compose URL and the reference IDs inserted by `R__development_seed.sql`.

## Start from a clean local database

The public API does not provide CRUD endpoints for dealerships, customers, vehicles, qualifications, service types, technicians, or bays. Consequently, Postman cannot seed those records over HTTP. Docker Compose already starts the application with `SPRING_PROFILES_ACTIVE=dev`; Flyway then applies `R__development_seed.sql` to a clean database.

```sh
docker compose down -v
docker compose up --build -d
```

Wait for `http://localhost:8080/actuator/health/readiness` to report `UP`. The collection's **Setup** folder initializes a unique future time window and verifies that the development seed is schedulable. Generated times are eight days in the future with per-run jitter, so normal reruns do not require database cleanup.

## Run in Postman

1. Import both JSON files.
2. Select **Unified Service Scheduler — Local Docker Compose** as the active environment.
3. Run the entire collection in its defined order with one iteration. For a video, the folder order tells the story: seed readiness, successful creation and retrieval, boundary-touching success, overlap rejection, validation/reference errors, then the race demonstration.
4. To show only the race, run **Concurrency Demo** with one iteration. Its harness makes two appointment POST calls asynchronously and proves that exactly one returns `201` and exactly one returns `409`.

When sending requests individually, send **Setup / Initialize run and verify readiness** first so the collection variables and fresh time windows are regenerated.

## Run with Newman

Install Newman if needed, then run the full regression suite:

```sh
newman run postman/Unified-Service-Scheduler.postman_collection.json \
  -e postman/Unified-Service-Scheduler.local.postman_environment.json \
  --iteration-count 1 --reporters cli,junit \
  --reporter-junit-export postman/newman-results.xml
```

Run the concurrency demonstration alone:

```sh
newman run postman/Unified-Service-Scheduler.postman_collection.json \
  -e postman/Unified-Service-Scheduler.local.postman_environment.json \
  --folder "Concurrency Demo" --iteration-count 1 \
  --reporters cli,junit \
  --reporter-junit-export postman/newman-concurrency.xml
```

Use exactly one Newman iteration. Newman iterations and ordinary collection items execute sequentially; increasing `--iteration-count` does not create concurrent HTTP calls. The race harness uses two concurrent `pm.sendRequest` calls with identical bodies/time windows and different idempotency keys, then aggregates both callbacks. This makes the `201 + 409` invariant testable in both Postman and Newman without relying on timing between separate sequential items.

## Implementation-specific expectations and current gaps

The suite deliberately asserts the service as implemented rather than inventing endpoints, request fields, or status codes:

| Requested scenario | Implemented API behavior captured by the suite |
| --- | --- |
| Missing required field | `400 INVALID_REQUEST`; the safe detail is `Request validation failed` and does not name the field. |
| Unknown service type | `404 RESOURCE_NOT_FOUND` with `Service type not found`, not `422`. |
| Time in the past | No past-time validation exists, so this is not included as an error regression. |
| Unqualified technician | The allocator returns combined `409 BOOKING_CONFLICT` with `No qualified technician and service bay are available`; it does not expose a distinct unqualified error. The single dev service has a matching qualified technician, so an unqualified-only fixture cannot be created through the public API. |
| Same technician/different bay or same bay/different technician | `POST /appointments` accepts no technician or bay IDs and the dev seed contains one of each. Independent resource conflicts cannot be targeted through the public contract. The overlap tests prove rejection for the assigned pair together. |
| Fully engulfing interval | Requests accept only `startTime`; duration comes from the service type. The sole dev service is 60 minutes, so a longer interval cannot be expressed over the 60-minute baseline. Both leading and trailing partial-overlap boundaries are covered instead. |
| Unknown dealership | Candidate allocation happens before dealership lookup, producing `409 BOOKING_CONFLICT` rather than the controller's documented `404`. The collection includes this as an explicit characterization test. |
| Conflict reason specificity | The production-safe response intentionally combines qualification, technician availability, and bay availability. It cannot identify a particular resource as the sole reason. |

These gaps are visible in request and folder descriptions so the collection remains useful in a demo while staying a passing CI regression suite for the current code.
