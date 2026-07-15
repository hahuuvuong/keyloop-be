ALTER TABLE appointment
    ADD CONSTRAINT ex_appointment_technician_overlap EXCLUDE USING gist (technician_id WITH =, tstzrange(start_time,end_time,'[)') WITH &&) WHERE (status='CONFIRMED');
ALTER TABLE appointment
    ADD CONSTRAINT ex_appointment_bay_overlap EXCLUDE USING gist (service_bay_id WITH =, tstzrange(start_time,end_time,'[)') WITH &&) WHERE (status='CONFIRMED');
