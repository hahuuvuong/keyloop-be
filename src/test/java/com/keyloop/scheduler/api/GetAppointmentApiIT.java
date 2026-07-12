package com.keyloop.scheduler.api;

import com.keyloop.scheduler.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class GetAppointmentApiIT extends PostgresIntegrationTest {
    @Autowired
    MockMvc mvc;

    @Test
    void unknownAppointmentReturnsSafe404() throws Exception {
        mvc.perform(get("/api/v1/appointments/{id}", UUID.randomUUID())).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }
}
