package com.keyloop.scheduler.appointment.api;

import io.swagger.v3.oas.models.*;
import org.springframework.context.annotation.*;

@Configuration
public class AppointmentOpenApi {
    @Bean
    OpenAPI schedulerOpenApi() {
        return new OpenAPI().info(new io.swagger.v3.oas.models.info.Info().title("Unified Service Scheduler").version("1.0.0").description("Atomic dealership service booking; availability checks are authoritative only during creation."));
    }
}
