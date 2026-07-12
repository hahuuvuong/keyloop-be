package com.keyloop.scheduler.appointment.api;
import jakarta.validation.constraints.*; import java.time.OffsetDateTime; import java.util.UUID;
public record CreateAppointmentRequest(@NotNull UUID customerId,@NotNull UUID vehicleId,@NotNull UUID dealershipId,@NotNull UUID serviceTypeId,@NotNull OffsetDateTime startTime) {}
