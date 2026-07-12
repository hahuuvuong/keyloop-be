package com.keyloop.scheduler.appointment.application;

import com.keyloop.scheduler.appointment.infrastructure.*;
import com.keyloop.scheduler.shared.error.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetAppointmentService {
    private final AppointmentRepository repository;

    public GetAppointmentService(AppointmentRepository r) {
        repository = r;
    }

    @Transactional(readOnly = true)
    public AppointmentView get(UUID id) {
        return repository.findDetailedById(id).map(AppointmentViews::from).orElseThrow(() -> new SchedulerException(ErrorCode.RESOURCE_NOT_FOUND, "Appointment not found"));
    }
}
