package com.keyloop.scheduler.api;

import com.fasterxml.jackson.databind.*;
import com.keyloop.scheduler.support.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class CreateAppointmentApiIT extends PostgresIntegrationTest {
    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    ObjectMapper json;

    @BeforeEach
    void seed() {
        SchedulingFixtures.reset(jdbc);
    }

    @Test
    void createsAndRetrievesAppointment() throws Exception {
        var body = "{\"customerId\":\"%s\",\"vehicleId\":\"%s\",\"dealershipId\":\"%s\",\"serviceTypeId\":\"%s\",\"startTime\":\"2030-01-01T10:00:00Z\"}".formatted(SchedulingFixtures.CUSTOMER, SchedulingFixtures.VEHICLE, SchedulingFixtures.DEALERSHIP, SchedulingFixtures.SERVICE);
        var result = mvc.perform(post("/api/v1/appointments").header("Idempotency-Key", "create-key-001").contentType("application/json").content(body)).andExpect(status().isCreated()).andExpect(header().exists("Location")).andExpect(jsonPath("$.status").value("CONFIRMED")).andReturn();
        mvc.perform(get(result.getResponse().getHeader("Location"))).andExpect(status().isOk()).andExpect(jsonPath("$.technician.id").value(SchedulingFixtures.TECHNICIAN.toString()));
        assertThatCount(1);
    }

    private void assertThatCount(int n) {
        org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject("select count(*) from appointment", Integer.class)).isEqualTo(n);
    }
}
