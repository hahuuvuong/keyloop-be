package com.keyloop.scheduler.appointment.application;

import com.keyloop.scheduler.appointment.domain.AppointmentInterval;
import com.keyloop.scheduler.appointment.infrastructure.AppointmentAllocationRepository;
import com.keyloop.scheduler.servicetype.infrastructure.ServiceTypeRepository;
import com.keyloop.scheduler.shared.error.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class AvailabilityService {
    private final ServiceTypeRepository services;
    private final AppointmentAllocationRepository allocation;

    public AvailabilityService(ServiceTypeRepository s, AppointmentAllocationRepository a) {
        services = s;
        allocation = a;
    }

    public boolean available(UUID dealership, UUID serviceType, Instant start) {
        var s = services.findById(serviceType).orElseThrow(() -> new SchedulerException(ErrorCode.RESOURCE_NOT_FOUND, "Service type not found"));
        var i = AppointmentInterval.startingAt(start, s.durationMinutes);
        return !allocation.candidates(dealership, s.requiredQualification.id, i.start(), i.end(), Set.of()).isEmpty();
    }
}
