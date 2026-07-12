package com.keyloop.scheduler.appointment.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class RequestFingerprint {
    private RequestFingerprint() {
    }

    public static String of(CreateAppointmentCommand c) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            var canonical = String.join("|", c.customerId().toString(), c.vehicleId().toString(), c.dealershipId().toString(), c.serviceTypeId().toString(), c.startTime().toString());
            return HexFormat.of().formatHex(md.digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
