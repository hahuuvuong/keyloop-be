package com.keyloop.scheduler.api;

import com.keyloop.scheduler.support.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class ListAppointmentsApiIT extends PostgresIntegrationTest {
    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void seed() {
        SchedulingFixtures.reset(jdbc);
    }

    @Test
    void listIncludesEveryAppointmentResourceReference() throws Exception {
        var body = "{\"customerId\":\"%s\",\"vehicleId\":\"%s\",\"dealershipId\":\"%s\",\"serviceTypeId\":\"%s\",\"startTime\":\"2030-01-01T10:00:00Z\"}"
                .formatted(SchedulingFixtures.CUSTOMER, SchedulingFixtures.VEHICLE,
                        SchedulingFixtures.DEALERSHIP, SchedulingFixtures.SERVICE);

        mvc.perform(post("/api/v1/appointments")
                        .header("Idempotency-Key", "list-reference-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/v1/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].customer.id").value(SchedulingFixtures.CUSTOMER.toString()))
                .andExpect(jsonPath("$.content[0].customer.uri").value("/api/v1/customers/" + SchedulingFixtures.CUSTOMER))
                .andExpect(jsonPath("$.content[0].vehicle.id").value(SchedulingFixtures.VEHICLE.toString()))
                .andExpect(jsonPath("$.content[0].vehicle.uri").value("/api/v1/vehicles/" + SchedulingFixtures.VEHICLE))
                .andExpect(jsonPath("$.content[0].dealership.id").value(SchedulingFixtures.DEALERSHIP.toString()))
                .andExpect(jsonPath("$.content[0].dealership.uri").value("/api/v1/dealerships/" + SchedulingFixtures.DEALERSHIP))
                .andExpect(jsonPath("$.content[0].serviceType.id").value(SchedulingFixtures.SERVICE.toString()))
                .andExpect(jsonPath("$.content[0].serviceType.uri").value("/api/v1/service-types/" + SchedulingFixtures.SERVICE))
                .andExpect(jsonPath("$.content[0].technician.id").value(SchedulingFixtures.TECHNICIAN.toString()))
                .andExpect(jsonPath("$.content[0].technician.uri").value("/api/v1/technicians/" + SchedulingFixtures.TECHNICIAN))
                .andExpect(jsonPath("$.content[0].serviceBay.id").value(SchedulingFixtures.BAY.toString()))
                .andExpect(jsonPath("$.content[0].serviceBay.uri").value("/api/v1/service-bays/" + SchedulingFixtures.BAY));
    }

    @Test
    void invalidRangeReturns400() throws Exception {
        mvc.perform(get("/api/v1/appointments").queryParam("startDate", "2030-01-02T00:00:00Z").queryParam("endDate", "2030-01-01T00:00:00Z")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }
}
