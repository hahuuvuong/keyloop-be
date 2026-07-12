package com.keyloop.scheduler.support;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

public final class SchedulingFixtures {
    public static final UUID DEALERSHIP = UUID.fromString("00000000-0000-0000-0000-000000000001"), CUSTOMER = UUID.fromString("00000000-0000-0000-0000-000000000002"), VEHICLE = UUID.fromString("00000000-0000-0000-0000-000000000003"), QUALIFICATION = UUID.fromString("00000000-0000-0000-0000-000000000004"), SERVICE = UUID.fromString("00000000-0000-0000-0000-000000000005"), TECHNICIAN = UUID.fromString("00000000-0000-0000-0000-000000000006"), BAY = UUID.fromString("00000000-0000-0000-0000-000000000007");

    private SchedulingFixtures() {
    }

    public static void reset(JdbcTemplate j) {
        j.update("delete from appointment_idempotency");
        j.update("delete from appointment");
        j.update("delete from technician_qualification");
        j.update("delete from technician");
        j.update("delete from service_bay");
        j.update("delete from service_type");
        j.update("delete from qualification");
        j.update("delete from vehicle");
        j.update("delete from customer");
        j.update("delete from dealership");
        j.update("insert into dealership values (?,?,?,true)", DEALERSHIP, "D1", "Dealership");
        j.update("insert into customer values (?,?,?)", CUSTOMER, "C1", "Customer");
        j.update("insert into vehicle values (?,?,?,?,?)", VEHICLE, CUSTOMER, "CAR-1", "Make", "Model");
        j.update("insert into qualification values (?,?,?)", QUALIFICATION, "Q1", "Qualification");
        j.update("insert into service_type values (?,?,?,?,?)", SERVICE, "S1", "Service", 60, QUALIFICATION);
        j.update("insert into technician values (?,?,?,?,true)", TECHNICIAN, DEALERSHIP, "T1", "Technician");
        j.update("insert into technician_qualification values (?,?)", TECHNICIAN, QUALIFICATION);
        j.update("insert into service_bay values (?,?,?,true)", BAY, DEALERSHIP, "B1");
    }
}
