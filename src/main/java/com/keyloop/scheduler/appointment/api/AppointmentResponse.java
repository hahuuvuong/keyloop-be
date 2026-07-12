package com.keyloop.scheduler.appointment.api;
import com.keyloop.scheduler.appointment.domain.AppointmentStatus; import java.time.Instant; import java.util.UUID;
public record AppointmentResponse(UUID id,ResourceReference customer,ResourceReference vehicle,ResourceReference dealership,ResourceReference serviceType,ResourceReference technician,ResourceReference serviceBay,Instant startTime,Instant endTime,AppointmentStatus status,Instant createdAt) {}
