package com.keyloop.scheduler.appointment.application;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class BookingMetrics {
    private final Counter attempts, confirmed, conflicts, failures;
    private final Timer duration;

    public BookingMetrics(MeterRegistry r) {
        attempts = r.counter("booking.attempts");
        confirmed = r.counter("booking.confirmations");
        conflicts = r.counter("booking.conflicts");
        failures = r.counter("booking.failures");
        duration = r.timer("booking.duration");
    }

    public <T> T measure(Supplier<T> s) {
        attempts.increment();
        return duration.record(s);
    }

    public void confirmed() {
        confirmed.increment();
    }

    public void conflict() {
        conflicts.increment();
    }

    public void failure() {
        failures.increment();
    }
}
