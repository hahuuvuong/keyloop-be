package com.keyloop.scheduler.api;

import com.keyloop.scheduler.support.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class AvailabilityPreviewApiIT extends PostgresIntegrationTest {
    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void seed() {
        SchedulingFixtures.reset(jdbc);
    }

    @Test
    void previewIsAdvisory() throws Exception {
        mvc.perform(get("/api/v1/availability").queryParam("dealershipId", SchedulingFixtures.DEALERSHIP.toString()).queryParam("serviceTypeId", SchedulingFixtures.SERVICE.toString()).queryParam("startTime", "2030-01-01T10:00:00Z")).andExpect(status().isOk()).andExpect(jsonPath("$.available").value(true)).andExpect(jsonPath("$.advisory").value(true));
    }
}
