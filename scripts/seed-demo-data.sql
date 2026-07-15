\set ON_ERROR_STOP on

-- Unified Service Scheduler demo data.
--
-- Run after Flyway has created the schema. Every insert is idempotent, so the
-- script can be executed repeatedly without duplicating rows. Fixed UUIDs make
-- the records easy to use from curl, Postman, or Swagger UI demonstrations.

BEGIN;

INSERT INTO dealership (id, code, name, active) VALUES
  ('00000000-0000-0000-0000-000000000001', 'BKK-01', 'Bangkok Central Dealership', true),
  ('10000000-0000-0000-0000-000000000001', 'HCM-01', 'Ho Chi Minh City Dealership', true)
ON CONFLICT DO NOTHING;

INSERT INTO customer (id, reference, display_name) VALUES
  ('00000000-0000-0000-0000-000000000002', 'CUS-001', 'Alex Morgan'),
  ('10000000-0000-0000-0000-000000000002', 'CUS-002', 'Jamie Lee'),
  ('10000000-0000-0000-0000-000000000003', 'CUS-003', 'Taylor Nguyen')
ON CONFLICT DO NOTHING;

INSERT INTO vehicle (id, customer_id, registration, make, model) VALUES
  ('00000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000002', 'DEMO-001', 'Toyota', 'Corolla'),
  ('10000000-0000-0000-0000-000000000011', '10000000-0000-0000-0000-000000000002', 'DEMO-002', 'Ford', 'Ranger'),
  ('10000000-0000-0000-0000-000000000012', '10000000-0000-0000-0000-000000000003', 'DEMO-003', 'Kia', 'EV6')
ON CONFLICT DO NOTHING;

INSERT INTO qualification (id, code, name) VALUES
  ('00000000-0000-0000-0000-000000000004', 'GENERAL', 'General Service'),
  ('10000000-0000-0000-0000-000000000021', 'DIAGNOSTICS', 'Vehicle Diagnostics'),
  ('10000000-0000-0000-0000-000000000022', 'EV', 'Electric Vehicle Service')
ON CONFLICT DO NOTHING;

INSERT INTO service_type (id, code, name, duration_minutes, required_qualification_id) VALUES
  ('00000000-0000-0000-0000-000000000005', 'STANDARD', 'Standard Service', 60, '00000000-0000-0000-0000-000000000004'),
  ('10000000-0000-0000-0000-000000000031', 'DIAGNOSTIC', 'Diagnostic Inspection', 90, '10000000-0000-0000-0000-000000000021'),
  ('10000000-0000-0000-0000-000000000032', 'EV-FULL', 'EV Full Service', 120, '10000000-0000-0000-0000-000000000022')
ON CONFLICT DO NOTHING;

INSERT INTO technician (id, dealership_id, employee_reference, display_name, active) VALUES
  ('00000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000001', 'TECH-001', 'Sam Technician', true),
  ('10000000-0000-0000-0000-000000000041', '00000000-0000-0000-0000-000000000001', 'TECH-002', 'Robin Specialist', true),
  ('10000000-0000-0000-0000-000000000042', '10000000-0000-0000-0000-000000000001', 'TECH-001', 'Minh Technician', true),
  ('10000000-0000-0000-0000-000000000043', '10000000-0000-0000-0000-000000000001', 'TECH-002', 'Inactive Demo Technician', false)
ON CONFLICT DO NOTHING;

INSERT INTO technician_qualification (technician_id, qualification_id) VALUES
  ('00000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000004'),
  ('00000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000021'),
  ('10000000-0000-0000-0000-000000000041', '00000000-0000-0000-0000-000000000004'),
  ('10000000-0000-0000-0000-000000000041', '10000000-0000-0000-0000-000000000022'),
  ('10000000-0000-0000-0000-000000000042', '00000000-0000-0000-0000-000000000004'),
  ('10000000-0000-0000-0000-000000000042', '10000000-0000-0000-0000-000000000021'),
  ('10000000-0000-0000-0000-000000000042', '10000000-0000-0000-0000-000000000022'),
  ('10000000-0000-0000-0000-000000000043', '00000000-0000-0000-0000-000000000004')
ON CONFLICT DO NOTHING;

INSERT INTO service_bay (id, dealership_id, code, active) VALUES
  ('00000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-000000000001', 'BAY-01', true),
  ('10000000-0000-0000-0000-000000000051', '00000000-0000-0000-0000-000000000001', 'BAY-02', true),
  ('10000000-0000-0000-0000-000000000052', '10000000-0000-0000-0000-000000000001', 'BAY-01', true),
  ('10000000-0000-0000-0000-000000000053', '10000000-0000-0000-0000-000000000001', 'BAY-02', false)
ON CONFLICT DO NOTHING;

-- Fixed future appointments keep demo output deterministic. The second starts
-- exactly when the first ends, demonstrating valid [start, end) boundaries.
-- The CANCELLED row overlaps them intentionally and does not block resources.
INSERT INTO appointment (
  id, customer_id, vehicle_id, dealership_id, service_type_id,
  technician_id, service_bay_id, start_time, end_time, status,
  created_at, cancelled_at
) VALUES
  (
    '20000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000002',
    '00000000-0000-0000-0000-000000000003',
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000005',
    '00000000-0000-0000-0000-000000000006',
    '00000000-0000-0000-0000-000000000007',
    '2035-01-15T09:00:00Z', '2035-01-15T10:00:00Z',
    'CONFIRMED', CURRENT_TIMESTAMP, NULL
  ),
  (
    '20000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000011',
    '00000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000031',
    '00000000-0000-0000-0000-000000000006',
    '00000000-0000-0000-0000-000000000007',
    '2035-01-15T10:00:00Z', '2035-01-15T11:30:00Z',
    'CONFIRMED', CURRENT_TIMESTAMP, NULL
  ),
  (
    '20000000-0000-0000-0000-000000000003',
    '10000000-0000-0000-0000-000000000003',
    '10000000-0000-0000-0000-000000000012',
    '10000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000032',
    '10000000-0000-0000-0000-000000000042',
    '10000000-0000-0000-0000-000000000052',
    '2035-01-16T13:00:00Z', '2035-01-16T15:00:00Z',
    'CONFIRMED', CURRENT_TIMESTAMP, NULL
  ),
  (
    '20000000-0000-0000-0000-000000000004',
    '00000000-0000-0000-0000-000000000002',
    '00000000-0000-0000-0000-000000000003',
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000005',
    '00000000-0000-0000-0000-000000000006',
    '00000000-0000-0000-0000-000000000007',
    '2035-01-15T09:30:00Z', '2035-01-15T10:30:00Z',
    'CANCELLED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  )
ON CONFLICT DO NOTHING;

-- The application normally stores a SHA-256 request fingerprint here. These
-- stable demo-only values satisfy the schema and make every appointment table
-- visible without pretending these rows came through the public POST endpoint.
INSERT INTO appointment_idempotency (
  idempotency_key, request_fingerprint, appointment_id, created_at
)
SELECT seed.idempotency_key, seed.request_fingerprint, seed.appointment_id, CURRENT_TIMESTAMP
FROM (VALUES
  ('demo-seed-appointment-001', repeat('1', 64), '20000000-0000-0000-0000-000000000001'::uuid),
  ('demo-seed-appointment-002', repeat('2', 64), '20000000-0000-0000-0000-000000000002'::uuid),
  ('demo-seed-appointment-003', repeat('3', 64), '20000000-0000-0000-0000-000000000003'::uuid),
  ('demo-seed-appointment-004', repeat('4', 64), '20000000-0000-0000-0000-000000000004'::uuid)
) AS seed(idempotency_key, request_fingerprint, appointment_id)
JOIN appointment existing ON existing.id = seed.appointment_id
ON CONFLICT DO NOTHING;

COMMIT;

-- Compact verification summary printed by psql.
SELECT 'dealership' AS table_name, count(*) AS row_count FROM dealership
UNION ALL SELECT 'customer', count(*) FROM customer
UNION ALL SELECT 'vehicle', count(*) FROM vehicle
UNION ALL SELECT 'qualification', count(*) FROM qualification
UNION ALL SELECT 'service_type', count(*) FROM service_type
UNION ALL SELECT 'technician', count(*) FROM technician
UNION ALL SELECT 'technician_qualification', count(*) FROM technician_qualification
UNION ALL SELECT 'service_bay', count(*) FROM service_bay
UNION ALL SELECT 'appointment', count(*) FROM appointment
UNION ALL SELECT 'appointment_idempotency', count(*) FROM appointment_idempotency
ORDER BY table_name;
