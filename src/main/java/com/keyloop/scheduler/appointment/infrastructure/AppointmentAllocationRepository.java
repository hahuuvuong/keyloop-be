package com.keyloop.scheduler.appointment.infrastructure;

import com.keyloop.scheduler.servicebay.infrastructure.*;
import com.keyloop.scheduler.technician.infrastructure.*;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;

@Repository
public class AppointmentAllocationRepository {
    private final TechnicianRepository technicians;
    private final ServiceBayRepository bays;

    public AppointmentAllocationRepository(TechnicianRepository t, ServiceBayRepository b) {
        technicians = t;
        bays = b;
    }

    public List<Candidate> candidates(UUID dealership, UUID qualification, Instant start, Instant end, Set<Candidate> excluded) {
        var ts = technicians.findEligible(dealership, qualification, start, end);
        var bs = bays.findEligible(dealership, start, end);
        var out = new ArrayList<Candidate>();
        for (var t : ts)
            for (var b : bs) {
                var c = new Candidate(t.id, b.id);
                if (!excluded.contains(c)) out.add(c);
            }
        return out;
    }

    public record Candidate(UUID technicianId, UUID bayId) {
    }
}
