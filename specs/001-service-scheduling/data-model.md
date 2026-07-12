# Data Model: Unified Service Scheduler

## Conventions

- UUID primary keys; `created_at` is required `timestamptz` in UTC.
- Names use bounded nonblank text. Durations are positive integer minutes.
- Foreign keys are required unless explicitly optional. Flyway owns all DDL.
- Appointment intervals satisfy `end_time > start_time` and use `[start,end)`.

## Tables

### dealership

`id` UUID PK; `name` varchar required; `code` varchar required unique; `active` boolean required.

### customer

`id` UUID PK; `reference` varchar required unique; minimal scenario-required display fields only.

### vehicle

`id` UUID PK; `customer_id` UUID required FK customer; `registration` varchar required unique;
minimal display fields. Add unique `(id,customer_id)` for ownership references and index `customer_id`.

### qualification

`id` UUID PK; `code` varchar required unique; `name` varchar required.

### service_type

`id` UUID PK; `code` varchar required unique; `name` varchar required; `duration_minutes` integer
required check `> 0`; `required_qualification_id` UUID required FK qualification. Index the FK.

### technician

`id` UUID PK; `dealership_id` UUID required FK dealership; `employee_reference` varchar required;
`display_name` varchar required; `active` boolean required; unique `(dealership_id,employee_reference)`;
index `(dealership_id,active,id)`.

### technician_qualification

`technician_id` UUID FK technician; `qualification_id` UUID FK qualification; composite PK;
reverse index `(qualification_id,technician_id)`.

### service_bay

`id` UUID PK; `dealership_id` UUID required FK dealership; `code` varchar required; `active` boolean
required; unique `(dealership_id,code)`; index `(dealership_id,active,id)`.

### appointment

`id` UUID PK; required FKs `customer_id`, `vehicle_id`, `dealership_id`, `service_type_id`,
`technician_id`, `service_bay_id`; `start_time` and `end_time` timestamptz required; `status` varchar
required check in (`CONFIRMED`,`CANCELLED`); `created_at` timestamptz required; optional `cancelled_at`
timestamptz; check `end_time > start_time`; check cancelled timestamp agrees with cancelled status.

Indexes: `(dealership_id,start_time,id)`, `(status,start_time)`, `(technician_id,start_time,end_time)`,
and `(service_bay_id,start_time,end_time)`. Foreign keys enforce associations. A composite FK
`(vehicle_id,customer_id) -> vehicle(id,customer_id)` provides database-level ownership enforcement
in addition to application validation.

Exclusion constraints after `CREATE EXTENSION IF NOT EXISTS btree_gist`:

```sql
EXCLUDE USING gist (
  technician_id WITH =,
  tstzrange(start_time, end_time, '[)') WITH &&
) WHERE (status = 'CONFIRMED');

EXCLUDE USING gist (
  service_bay_id WITH =,
  tstzrange(start_time, end_time, '[)') WITH &&
) WHERE (status = 'CONFIRMED');
```

Use stable internal names recorded only in infrastructure exception mapping; never expose them publicly.

### appointment_idempotency

`idempotency_key` varchar PK; `request_fingerprint` char(64) required; `appointment_id` UUID required
unique FK appointment; `created_at` timestamptz required. Key length and allowed-character checks bound
input. The row is written atomically with the appointment. A uniqueness race is re-read: matching
fingerprint returns the existing appointment; mismatching fingerprint is an invalid-request outcome.

## Relationships

- Customer 1—many Vehicle and Appointment.
- Dealership 1—many Technician, Service Bay, and Appointment.
- Qualification many—many Technician; Qualification 1—many Service Type.
- Service Type, Technician, and Service Bay each 1—many Appointment.
- Appointment 1—0..1 Idempotency record (required for appointments created through the public POST).

## Domain Rules and State

An appointment is created directly as `CONFIRMED`. `CONFIRMED -> CANCELLED` is the only anticipated
transition; no cancellation use case is in scope. Confirmed rows block resources; cancelled rows do not.
No optimistic version is added: exclusion constraints protect allocation, and no in-scope concurrent
update workflow benefits from entity versioning.

## Availability Query

Eligible technician/bay queries filter dealership and active status, join required qualification for
technicians, and exclude a blocking appointment satisfying:

```text
existing.start_time < requested.end_time
AND existing.end_time > requested.start_time
AND existing.status = CONFIRMED
```

Candidates order by UUID (or deterministic seeded identifier). This query improves selection and error
quality; exclusion constraints remain authoritative.

## Migration and Seed Separation

- Versioned production migrations create extensions, tables, constraints, and indexes.
- Development data is loaded only through an explicit `dev` profile seed location or a documented seed
  command, never from the production migration location.
- Tests use dedicated deterministic fixtures after migrations, not production-coupled seed scripts.
