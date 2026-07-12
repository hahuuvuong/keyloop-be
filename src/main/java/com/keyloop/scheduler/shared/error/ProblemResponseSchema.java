package com.keyloop.scheduler.shared.error;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "ProblemResponse", description = "Machine-readable RFC 9457 error response")
public record ProblemResponseSchema(
        @Schema(example = "409") int status,
        @Schema(example = "BOOKING_CONFLICT") String code,
        @Schema(example = "Appointment conflict") String title,
        @Schema(example = "No suitable technician and service bay are available") String detail,
        @Schema(example = "/api/v1/appointments") String instance,
        @Schema(example = "2030-01-01T10:00:00Z") Instant timestamp,
        @Schema(example = "8b14f35a-67ab-4f4d-b5b8-5b01f36a7c72") String correlationId) {
}
