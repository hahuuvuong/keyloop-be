package com.keyloop.scheduler.api;

import com.keyloop.scheduler.support.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class ListAppointmentsApiIT extends PostgresIntegrationTest {
    @Autowired
    MockMvc mvc;

    @Test
    void invalidRangeReturns400() throws Exception {
        mvc.perform(get("/api/v1/appointments").queryParam("startDate", "2030-01-02T00:00:00Z").queryParam("endDate", "2030-01-01T00:00:00Z")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }
}
