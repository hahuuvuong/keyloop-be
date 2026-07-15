CREATE TABLE appointment
(
    id              uuid PRIMARY KEY,
    customer_id     uuid        NOT NULL REFERENCES customer (id),
    vehicle_id      uuid        NOT NULL,
    dealership_id   uuid        NOT NULL REFERENCES dealership (id),
    service_type_id uuid        NOT NULL REFERENCES service_type (id),
    technician_id   uuid        NOT NULL REFERENCES technician (id),
    service_bay_id  uuid        NOT NULL REFERENCES service_bay (id),
    start_time      timestamptz NOT NULL,
    end_time        timestamptz NOT NULL,
    status          varchar(20) NOT NULL CHECK (status IN ('CONFIRMED', 'CANCELLED')),
    created_at      timestamptz NOT NULL,
    cancelled_at    timestamptz,
    CONSTRAINT fk_appointment_vehicle_owner FOREIGN KEY (vehicle_id, customer_id) REFERENCES vehicle (id, customer_id),
    CONSTRAINT ck_appointment_interval CHECK (end_time > start_time),
    CONSTRAINT ck_appointment_cancelled CHECK ((status = 'CANCELLED' AND cancelled_at IS NOT NULL) OR
                                               (status = 'CONFIRMED' AND cancelled_at IS NULL))
);
CREATE INDEX idx_appointment_dealership_start ON appointment (dealership_id, start_time, id);
CREATE INDEX idx_appointment_status_start ON appointment (status, start_time);
CREATE INDEX idx_appointment_technician_interval ON appointment (technician_id, start_time, end_time);
CREATE INDEX idx_appointment_bay_interval ON appointment (service_bay_id, start_time, end_time);
CREATE TABLE appointment_idempotency
(
    idempotency_key     varchar(128) PRIMARY KEY CHECK (length(idempotency_key) >= 8),
    request_fingerprint char(64)    NOT NULL,
    appointment_id      uuid        NOT NULL UNIQUE REFERENCES appointment (id),
    created_at          timestamptz NOT NULL
);
