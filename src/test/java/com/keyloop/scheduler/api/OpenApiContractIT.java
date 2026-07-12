package com.keyloop.scheduler.api;

import com.keyloop.scheduler.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class OpenApiContractIT extends PostgresIntegrationTest {
    @Autowired
    MockMvc mvc;

    @Test
    void publishesRequiredPaths() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(jsonPath("$.paths['/api/v1/appointments']").exists()).andExpect(jsonPath("$.paths['/api/v1/appointments/{appointmentId}']").exists()).andExpect(jsonPath("$.components.schemas.ProblemResponse.properties.instance").exists()).andExpect(jsonPath("$.components.schemas.ProblemResponse.properties.path").doesNotExist());
    }
}
