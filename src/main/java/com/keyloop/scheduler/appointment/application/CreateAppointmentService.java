package com.keyloop.scheduler.appointment.application;

import com.keyloop.scheduler.shared.error.*;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class CreateAppointmentService {
    private final AppointmentBookingCoordinator coordinator;
    private final IdempotencyRecoveryService recovery;
    private final BookingMetrics metrics;

    public CreateAppointmentService(AppointmentBookingCoordinator c, IdempotencyRecoveryService r, BookingMetrics m) {
        coordinator = c;
        recovery = r;
        metrics = m;
    }

    public AppointmentView create(CreateAppointmentCommand command) {
        if (command.idempotencyKey() == null || command.idempotencyKey().length() < 8)
            throw new SchedulerException(ErrorCode.INVALID_REQUEST, "Idempotency-Key must contain at least 8 characters");
        var fp = RequestFingerprint.of(command);
        var existing = recovery.find(command.idempotencyKey(), fp);
        if (existing.isPresent()) return existing.get();
        try {
            var result = metrics.measure(() -> coordinator.book(command, fp));
            metrics.confirmed();
            return result;
        } catch (DataIntegrityViolationException ex) {
            var recovered = recovery.find(command.idempotencyKey(), fp);
            if (recovered.isPresent()) return recovered.get();
            metrics.conflict();
            throw new SchedulerException(ErrorCode.BOOKING_RACE_LOST, "A competing request won the booking race");
        } catch (ConcurrencyFailureException ex) {
            var recovered = recovery.find(command.idempotencyKey(), fp);
            if (recovered.isPresent()) return recovered.get();
            metrics.conflict();
            throw new SchedulerException(ErrorCode.BOOKING_RACE_LOST, "A competing request won the booking race");
        } catch (SchedulerException ex) {
            if (ex.code() == ErrorCode.BOOKING_CONFLICT) {
                var recovered = recovery.find(command.idempotencyKey(), fp);
                if (recovered.isPresent()) return recovered.get();
                metrics.conflict();
            } else metrics.failure();
            throw ex;
        } catch (RuntimeException ex) {
            metrics.failure();
            throw ex;
        }
    }
}
