package com.keyloop.scheduler.integration;

import com.keyloop.scheduler.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class ObservabilityIT extends PostgresIntegrationTest {
    @Autowired
    MockMvc mvc;

    @Test
    void healthAndCorrelationAreExposed() throws Exception {
        mvc.perform(get("/actuator/health").header("X-Correlation-ID", "test-correlation")).andExpect(status().isOk()).andExpect(header().string("X-Correlation-ID", "test-correlation"));
    }
}
