# Architecture and Data Flow

```text
HTTP -> API records/controller -> CreateAppointmentService
                              -> AppointmentBookingCoordinator
                              -> TransactionalBookingAttempt (REQUIRES_NEW)
                              -> JPA repositories -> PostgreSQL/Flyway constraints
```

```text
Customer 1--* Vehicle
Dealership 1--* Technician *--* Qualification 1--* ServiceType
Dealership 1--* ServiceBay
Appointment *--1 Customer, Vehicle, Dealership, ServiceType, Technician, ServiceBay
Appointment 1--0..1 Idempotency
```

Booking validates references/ownership, calculates the UTC `[start,end)` interval, selects candidates
in UUID order, performs one atomic attempt, flushes both appointment and idempotency record, and returns
only after commit. A recognized overlap rolls back before an alternative candidate is tried.
