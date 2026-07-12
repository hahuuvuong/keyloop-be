package com.keyloop.scheduler.appointment.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public record AppointmentInterval(Instant start, Instant end) {
    public AppointmentInterval {
        Objects.requireNonNull(start); Objects.requireNonNull(end);
        if (!end.isAfter(start)) throw new IllegalArgumentException("End must be after start");
    }
    public static AppointmentInterval startingAt(Instant start, int minutes) {
        if (minutes <= 0) throw new IllegalArgumentException("Duration must be positive");
        return new AppointmentInterval(start, start.plus(Duration.ofMinutes(minutes)));
    }
    public boolean overlaps(AppointmentInterval other) { return start.isBefore(other.end) && other.start.isBefore(end); }
}
