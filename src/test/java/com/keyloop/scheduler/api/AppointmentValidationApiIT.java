package com.keyloop.scheduler.api;

import com.keyloop.scheduler.support.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class AppointmentValidationApiIT extends PostgresIntegrationTest {
    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void seed() {
        SchedulingFixtures.reset(jdbc);
    }

    @Test
    void malformedRequestReturnsSafe400() throws Exception {
        mvc.perform(post("/api/v1/appointments").header("Idempotency-Key", "invalid-001").contentType("application/json").content("{}")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST")).andExpect(header().exists("X-Correlation-ID"));
    }

    @Test
    void inconsistentVehicleOwnerReturns400() throws Exception {
        var other = java.util.UUID.fromString("00000000-0000-0000-0000-000000000099");
        jdbc.update("insert into customer values (?,?,?)", other, "C2", "Other Customer");
        var body = "{\"customerId\":\"%s\",\"vehicleId\":\"%s\",\"dealershipId\":\"%s\",\"serviceTypeId\":\"%s\",\"startTime\":\"2030-01-01T10:00:00Z\"}".formatted(other, SchedulingFixtures.VEHICLE, SchedulingFixtures.DEALERSHIP, SchedulingFixtures.SERVICE);
        mvc.perform(post("/api/v1/appointments").header("Idempotency-Key", "owner-001").contentType("application/json").content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject("select count(*) from appointment", Integer.class)).isZero();
    }

    @Test
    void unknownReferenceReturns404() throws Exception {
        var body = "{\"customerId\":\"%s\",\"vehicleId\":\"%s\",\"dealershipId\":\"%s\",\"serviceTypeId\":\"%s\",\"startTime\":\"2030-01-01T10:00:00Z\"}".formatted(SchedulingFixtures.CUSTOMER, SchedulingFixtures.VEHICLE, SchedulingFixtures.DEALERSHIP, java.util.UUID.randomUUID());
        mvc.perform(post("/api/v1/appointments").header("Idempotency-Key", "missing-001").contentType("application/json").content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }
}
