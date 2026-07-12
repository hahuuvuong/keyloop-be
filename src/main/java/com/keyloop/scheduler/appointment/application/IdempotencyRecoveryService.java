package com.keyloop.scheduler.appointment.application;

import com.keyloop.scheduler.appointment.infrastructure.*;
import com.keyloop.scheduler.shared.error.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

import java.util.Optional;

@Service
public class IdempotencyRecoveryService {
    private final IdempotencyRepository keys;

    public IdempotencyRecoveryService(IdempotencyRepository k) {
        keys = k;
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Optional<AppointmentView> find(String key, String fingerprint) {
        return keys.findDetailedByKey(key).map(row -> {
            if (!row.fingerprint.trim().equals(fingerprint))
                throw new SchedulerException(ErrorCode.IDEMPOTENCY_KEY_REUSED, "Idempotency key was used for a different request");
            return AppointmentViews.from(row.appointment);
        });
    }
}
