package com.keyloop.scheduler.appointment.application;

import com.keyloop.scheduler.appointment.domain.AppointmentInterval;
import com.keyloop.scheduler.appointment.infrastructure.*;
import com.keyloop.scheduler.servicetype.infrastructure.*;
import com.keyloop.scheduler.shared.error.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AppointmentBookingCoordinator {
    private final ServiceTypeRepository services;
    private final AppointmentAllocationRepository allocation;
    private final TransactionalBookingAttempt attempt;

    public AppointmentBookingCoordinator(ServiceTypeRepository s, AppointmentAllocationRepository a, TransactionalBookingAttempt t) {
        services = s;
        allocation = a;
        attempt = t;
    }

    public AppointmentView book(CreateAppointmentCommand command, String fingerprint) {
        var service = services.findById(command.serviceTypeId()).orElseThrow(() -> new SchedulerException(ErrorCode.RESOURCE_NOT_FOUND, "Service type not found"));
        var interval = AppointmentInterval.startingAt(command.startTime(), service.durationMinutes);
        var excluded = new HashSet<AppointmentAllocationRepository.Candidate>();
        for (int i = 0; i < 3; i++) {
            var candidates = allocation.candidates(command.dealershipId(), service.requiredQualification.id, interval.start(), interval.end(), excluded);
            if (candidates.isEmpty()) break;
            var c = candidates.getFirst();
            try {
                return attempt.attempt(command, c, fingerprint);
            } catch (DataIntegrityViolationException ex) {
                var message = rootMessage(ex);
                if (!message.contains("ex_appointment_technician_overlap") && !message.contains("ex_appointment_bay_overlap"))
                    throw ex;
                excluded.add(c);
            }
        }
        throw new SchedulerException(ErrorCode.BOOKING_CONFLICT, "No qualified technician and service bay are available");
    }

    private static String rootMessage(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        return String.valueOf(t.getMessage());
    }
}
