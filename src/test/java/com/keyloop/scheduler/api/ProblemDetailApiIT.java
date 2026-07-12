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
class ProblemDetailApiIT extends PostgresIntegrationTest {
    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void seed() {
        SchedulingFixtures.reset(jdbc);
        jdbc.update("update technician set active=false");
    }

    @Test
    void noResourcesReturnsConflictWithoutResidue() throws Exception {
        var body = "{\"customerId\":\"%s\",\"vehicleId\":\"%s\",\"dealershipId\":\"%s\",\"serviceTypeId\":\"%s\",\"startTime\":\"2030-01-01T10:00:00Z\"}".formatted(SchedulingFixtures.CUSTOMER, SchedulingFixtures.VEHICLE, SchedulingFixtures.DEALERSHIP, SchedulingFixtures.SERVICE);
        mvc.perform(post("/api/v1/appointments").header("Idempotency-Key", "conflict-001").contentType("application/json").content(body)).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("BOOKING_CONFLICT"));
        org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject("select count(*) from appointment", Integer.class)).isZero();
    }
}
