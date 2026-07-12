package com.keyloop.scheduler.appointment.api;

import com.keyloop.scheduler.appointment.application.AvailabilityService;
import com.keyloop.scheduler.shared.error.ProblemResponseSchema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/availability")
public class AvailabilityController {
    private final AvailabilityService service;
    public AvailabilityController(AvailabilityService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Preview resource availability", description = "Advisory only; appointment creation revalidates availability atomically.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Advisory availability result"), @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ProblemResponseSchema.class))), @ApiResponse(responseCode = "404", description = "Referenced record not found", content = @Content(schema = @Schema(implementation = ProblemResponseSchema.class))), @ApiResponse(responseCode = "500", description = "Unexpected failure", content = @Content(schema = @Schema(implementation = ProblemResponseSchema.class)))})
    public Map<String, Boolean> preview(@RequestParam UUID dealershipId, @RequestParam UUID serviceTypeId, @RequestParam OffsetDateTime startTime) {
        return Map.of("available", service.available(dealershipId, serviceTypeId, startTime.toInstant()), "advisory", true);
    }
}
