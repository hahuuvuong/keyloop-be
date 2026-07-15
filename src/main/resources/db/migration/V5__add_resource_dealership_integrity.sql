ALTER TABLE technician
    ADD CONSTRAINT uq_technician_id_dealership UNIQUE (id, dealership_id);
ALTER TABLE service_bay
    ADD CONSTRAINT uq_service_bay_id_dealership UNIQUE (id, dealership_id);

ALTER TABLE appointment
    ADD CONSTRAINT fk_appointment_technician_dealership
        FOREIGN KEY (technician_id, dealership_id) REFERENCES technician (id, dealership_id);

ALTER TABLE appointment
    ADD CONSTRAINT fk_appointment_bay_dealership
        FOREIGN KEY (service_bay_id, dealership_id) REFERENCES service_bay (id, dealership_id);
