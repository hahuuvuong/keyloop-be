package com.keyloop.scheduler.appointment.api;

import com.keyloop.scheduler.appointment.application.*;
import com.keyloop.scheduler.appointment.domain.AppointmentStatus;
import com.keyloop.scheduler.shared.error.ProblemResponseSchema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {
    private final CreateAppointmentService create;
    private final GetAppointmentService get;
    private final ListAppointmentsService list;

    public AppointmentController(CreateAppointmentService c, GetAppointmentService g, ListAppointmentsService l) {
        create = c;
        get = g;
        list = l;
    }

    @PostMapping
    @Operation(summary = "Create and confirm an appointment atomically")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Appointment confirmed", headers = @Header(name = "Location", description = "URI of the confirmed appointment")),
            @ApiResponse(responseCode = "400", description = "Malformed or invalid request", content = @Content(schema = @Schema(implementation = ProblemResponseSchema.class))),
            @ApiResponse(responseCode = "404", description = "Referenced record not found", content = @Content(schema = @Schema(implementation = ProblemResponseSchema.class))),
            @ApiResponse(responseCode = "409", description = "Resources unavailable or a concurrent request won", content = @Content(schema = @Schema(implementation = ProblemResponseSchema.class), examples = {
                    @ExampleObject(name = "unavailable", value = "{\"status\":409,\"code\":\"BOOKING_CONFLICT\",\"title\":\"Conflict\",\"detail\":\"No qualified technician and service bay are available\",\"instance\":\"/api/v1/appointments\",\"timestamp\":\"2030-01-01T10:00:00Z\",\"correlationId\":\"8b14f35a-67ab-4f4d-b5b8-5b01f36a7c72\"}"),
                    @ExampleObject(name = "concurrentWinner", value = "{\"status\":409,\"code\":\"BOOKING_RACE_LOST\",\"title\":\"Conflict\",\"detail\":\"A competing request won the booking race\",\"instance\":\"/api/v1/appointments\",\"timestamp\":\"2030-01-01T10:00:00Z\",\"correlationId\":\"8b14f35a-67ab-4f4d-b5b8-5b01f36a7c72\"}")
            })),
            @ApiResponse(responseCode = "500", description = "Unexpected operational failure", content = @Content(schema = @Schema(implementation = ProblemResponseSchema.class)))
    })
    public ResponseEntity<AppointmentResponse> create(@RequestHeader("Idempotency-Key") @Size(min = 8, max = 128) String key, @Valid @RequestBody CreateAppointmentRequest request) {
        var view = create.create(new CreateAppointmentCommand(request.customerId(), request.vehicleId(), request.dealershipId(), request.serviceTypeId(), request.startTime().toInstant(), key));
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(view.id()).toUri();
        return ResponseEntity.created(location).body(AppointmentMapper.toResponse(view));
    }

    @GetMapping("/{appointmentId}")
    @Operation(summary = "Retrieve a confirmed appointment")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Appointment found"), @ApiResponse(responseCode = "404", description = "Appointment not found", content = @Content(schema = @Schema(implementation = ProblemResponseSchema.class))), @ApiResponse(responseCode = "400", description = "Invalid identifier", content = @Content(schema = @Schema(implementation = ProblemResponseSchema.class))), @ApiResponse(responseCode = "500", description = "Unexpected failure", content = @Content(schema = @Schema(implementation = ProblemResponseSchema.class)))})
    public AppointmentResponse get(@PathVariable UUID appointmentId) {
        return AppointmentMapper.toResponse(get.get(appointmentId));
    }

    @GetMapping
    @Operation(summary = "List appointments with optional filters, pagination, and sorting")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Page of appointments"), @ApiResponse(responseCode = "400", description = "Invalid filter or sort", content = @Content(schema = @Schema(implementation = ProblemResponseSchema.class))), @ApiResponse(responseCode = "500", description = "Unexpected failure", content = @Content(schema = @Schema(implementation = ProblemResponseSchema.class)))})
    public PageResponse<AppointmentResponse> list(@RequestParam(required = false) UUID dealershipId, @RequestParam(required = false) OffsetDateTime startDate, @RequestParam(required = false) OffsetDateTime endDate, @RequestParam(required = false) AppointmentStatus status, @RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size, @RequestParam(defaultValue = "startTime,asc") String sort) {
        var parts = sort.split(",", 2);
        if (!Set.of("startTime", "createdAt", "id").contains(parts[0]))
            throw new IllegalArgumentException("Unsupported sort field");
        if (parts.length > 1 && !Set.of("asc", "desc").contains(parts[1].toLowerCase(Locale.ROOT)))
            throw new IllegalArgumentException("Unsupported sort direction");
        var direction = parts.length > 1 && parts[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        var result = list.list(dealershipId, startDate == null ? null : startDate.toInstant(), endDate == null ? null : endDate.toInstant(), status, PageRequest.of(page, size, Sort.by(direction, parts[0]).and(Sort.by("id")))).map(AppointmentMapper::toResponse);
        return PageResponse.from(result);
    }
}
