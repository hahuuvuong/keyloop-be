package com.keyloop.scheduler.appointment.application;

import java.time.Instant;
import java.util.UUID;

public record CreateAppointmentCommand(UUID customerId, UUID vehicleId, UUID dealershipId, UUID serviceTypeId,
                                       Instant startTime, String idempotencyKey) {
}
