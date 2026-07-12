package com.keyloop.scheduler.appointment.application;

import org.slf4j.*;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BookingAuditLogger {
    private static final Logger log = LoggerFactory.getLogger(BookingAuditLogger.class);

    public void confirmed(UUID id) {
        log.info("booking outcome=confirmed appointmentId={}", id);
    }

    public void conflict(String code) {
        log.warn("booking outcome=conflict code={}", code);
    }

    public void failure() {
        log.error("booking outcome=failure");
    }
}
