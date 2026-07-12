package com.keyloop.scheduler.appointment.application;

import com.keyloop.scheduler.appointment.domain.AppointmentStatus;

import java.time.Instant;
import java.util.UUID;

public record AppointmentView(UUID id, UUID customerId, UUID vehicleId, UUID dealershipId, UUID serviceTypeId,
                              UUID technicianId, UUID serviceBayId, Instant startTime, Instant endTime,
                              AppointmentStatus status, Instant createdAt) {
}
