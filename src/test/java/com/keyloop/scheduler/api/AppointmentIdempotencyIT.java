package com.keyloop.scheduler.api;

import com.keyloop.scheduler.support.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.Executors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureMockMvc
class AppointmentIdempotencyIT extends PostgresIntegrationTest {
    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void seed() {
        SchedulingFixtures.reset(jdbc);
    }

    @Test
    void tenReplaysCreateOneRow() throws Exception {
        var body = "{\"customerId\":\"%s\",\"vehicleId\":\"%s\",\"dealershipId\":\"%s\",\"serviceTypeId\":\"%s\",\"startTime\":\"2030-01-01T10:00:00Z\"}".formatted(SchedulingFixtures.CUSTOMER, SchedulingFixtures.VEHICLE, SchedulingFixtures.DEALERSHIP, SchedulingFixtures.SERVICE);
        String location = null;
        String appointmentId = null;
        for (int i = 0; i < 10; i++) {
            var response = mvc.perform(post("/api/v1/appointments").header("Idempotency-Key", "replay-key-001").contentType("application/json").content(body))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andReturn().getResponse();
            var id = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response.getContentAsString()).get("id").asText();
            if (location == null) {
                location = response.getHeader("Location");
                appointmentId = id;
            }
            assertThat(response.getHeader("Location")).isEqualTo(location);
            assertThat(id).isEqualTo(appointmentId);
        }
        assertThat(jdbc.queryForObject("select count(*) from appointment", Integer.class)).isOne();
    }

    @Test
    void sameKeyWithDifferentPayloadIsRejected() throws Exception {
        var first = "{\"customerId\":\"%s\",\"vehicleId\":\"%s\",\"dealershipId\":\"%s\",\"serviceTypeId\":\"%s\",\"startTime\":\"2030-01-01T10:00:00Z\"}".formatted(SchedulingFixtures.CUSTOMER, SchedulingFixtures.VEHICLE, SchedulingFixtures.DEALERSHIP, SchedulingFixtures.SERVICE);
        var changed = "{\"customerId\":\"%s\",\"vehicleId\":\"%s\",\"dealershipId\":\"%s\",\"serviceTypeId\":\"%s\",\"startTime\":\"2030-01-01T11:00:00Z\"}".formatted(SchedulingFixtures.CUSTOMER, SchedulingFixtures.VEHICLE, SchedulingFixtures.DEALERSHIP, SchedulingFixtures.SERVICE);
        mvc.perform(post("/api/v1/appointments").header("Idempotency-Key", "mismatch-key-001").contentType("application/json").content(first)).andExpect(status().isCreated());
        mvc.perform(post("/api/v1/appointments").header("Idempotency-Key", "mismatch-key-001").contentType("application/json").content(changed))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSED"));
        assertThat(jdbc.queryForObject("select count(*) from appointment", Integer.class)).isOne();
    }

    @Test
    void concurrentSameKeyReplaysCreateOneRow() throws Exception {
        var body = "{\"customerId\":\"%s\",\"vehicleId\":\"%s\",\"dealershipId\":\"%s\",\"serviceTypeId\":\"%s\",\"startTime\":\"2030-01-02T10:00:00Z\"}".formatted(SchedulingFixtures.CUSTOMER, SchedulingFixtures.VEHICLE, SchedulingFixtures.DEALERSHIP, SchedulingFixtures.SERVICE);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var a = executor.submit(() -> mvc.perform(post("/api/v1/appointments").header("Idempotency-Key", "same-key-001").contentType("application/json").content(body)).andReturn().getResponse().getStatus());
            var b = executor.submit(() -> mvc.perform(post("/api/v1/appointments").header("Idempotency-Key", "same-key-001").contentType("application/json").content(body)).andReturn().getResponse().getStatus());
            assertThat(java.util.List.of(a.get(), b.get())).allMatch(status -> status == 201);
        }
        assertThat(jdbc.queryForObject("select count(*) from appointment", Integer.class)).isOne();
    }
}
