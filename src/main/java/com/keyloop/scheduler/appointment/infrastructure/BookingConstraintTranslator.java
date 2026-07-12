package com.keyloop.scheduler.appointment.infrastructure;

import com.keyloop.scheduler.shared.error.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class BookingConstraintTranslator {
    public SchedulerException translate(DataIntegrityViolationException e) {
        var t = (Throwable) e;
        while (t.getCause() != null) t = t.getCause();
        var m = String.valueOf(t.getMessage());
        if (m.contains("ex_appointment_technician_overlap") || m.contains("ex_appointment_bay_overlap"))
            return new SchedulerException(ErrorCode.BOOKING_RACE_LOST, "A competing request won the booking race");
        throw e;
    }
}
