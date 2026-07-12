package com.keyloop.scheduler.appointment.application;

import com.keyloop.scheduler.appointment.domain.AppointmentStatus;
import com.keyloop.scheduler.appointment.infrastructure.*;
import com.keyloop.scheduler.shared.error.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ListAppointmentsService {
    private final AppointmentRepository repository;

    public ListAppointmentsService(AppointmentRepository r) {
        repository = r;
    }

    @Transactional(readOnly = true)
    public Page<AppointmentView> list(UUID dealership, Instant start, Instant end, AppointmentStatus status, Pageable pageable) {
        if (start != null && end != null && !end.isAfter(start))
            throw new SchedulerException(ErrorCode.INVALID_REQUEST, "endDate must be after startDate");
        Specification<AppointmentEntity> s = Specification.where(null);
        if (dealership != null) s = s.and((r, q, b) -> b.equal(r.get("dealership").get("id"), dealership));
        if (start != null) s = s.and((r, q, b) -> b.greaterThanOrEqualTo(r.get("startTime"), start));
        if (end != null) s = s.and((r, q, b) -> b.lessThan(r.get("startTime"), end));
        if (status != null) s = s.and((r, q, b) -> b.equal(r.get("status"), status));
        return repository.findAll(s, pageable).map(AppointmentViews::from);
    }
}
