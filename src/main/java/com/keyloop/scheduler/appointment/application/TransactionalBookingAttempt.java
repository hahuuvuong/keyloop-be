package com.keyloop.scheduler.appointment.application;

import com.keyloop.scheduler.appointment.domain.*;
import com.keyloop.scheduler.appointment.infrastructure.*;
import com.keyloop.scheduler.customer.infrastructure.*;
import com.keyloop.scheduler.vehicle.infrastructure.*;
import com.keyloop.scheduler.dealership.infrastructure.*;
import com.keyloop.scheduler.servicetype.infrastructure.*;
import com.keyloop.scheduler.technician.infrastructure.*;
import com.keyloop.scheduler.servicebay.infrastructure.*;
import com.keyloop.scheduler.shared.error.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

import java.time.*;
import java.util.*;

@Service
public class TransactionalBookingAttempt {
    private final CustomerRepository customers;
    private final VehicleRepository vehicles;
    private final DealershipRepository dealerships;
    private final ServiceTypeRepository services;
    private final TechnicianRepository technicians;
    private final ServiceBayRepository bays;
    private final AppointmentRepository appointments;
    private final IdempotencyRepository keys;

    public TransactionalBookingAttempt(CustomerRepository c, VehicleRepository v, DealershipRepository d, ServiceTypeRepository s, TechnicianRepository t, ServiceBayRepository b, AppointmentRepository a, IdempotencyRepository k) {
        customers = c;
        vehicles = v;
        dealerships = d;
        services = s;
        technicians = t;
        bays = b;
        appointments = a;
        keys = k;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AppointmentView attempt(CreateAppointmentCommand command, AppointmentAllocationRepository.Candidate candidate, String fingerprint) {
        var customer = customers.findById(command.customerId()).orElseThrow(() -> new SchedulerException(ErrorCode.RESOURCE_NOT_FOUND, "Customer not found"));
        var vehicle = vehicles.findById(command.vehicleId()).orElseThrow(() -> new SchedulerException(ErrorCode.RESOURCE_NOT_FOUND, "Vehicle not found"));
        if (!vehicles.existsByIdAndCustomerId(vehicle.id, customer.id))
            throw new SchedulerException(ErrorCode.INVALID_REQUEST, "Vehicle does not belong to customer");
        var dealership = dealerships.findById(command.dealershipId()).orElseThrow(() -> new SchedulerException(ErrorCode.RESOURCE_NOT_FOUND, "Dealership not found"));
        var service = services.findById(command.serviceTypeId()).orElseThrow(() -> new SchedulerException(ErrorCode.RESOURCE_NOT_FOUND, "Service type not found"));
        var technician = technicians.findById(candidate.technicianId()).orElseThrow();
        var bay = bays.findById(candidate.bayId()).orElseThrow();
        var interval = AppointmentInterval.startingAt(command.startTime(), service.durationMinutes);
        var a = new AppointmentEntity(UUID.randomUUID());
        a.customer = customer;
        a.vehicleId = vehicle.id;
        a.vehicle = vehicle;
        a.dealership = dealership;
        a.serviceType = service;
        a.technician = technician;
        a.serviceBay = bay;
        a.startTime = interval.start();
        a.endTime = interval.end();
        a.status = AppointmentStatus.CONFIRMED;
        a.createdAt = Instant.now();
        appointments.save(a);
        var key = new IdempotencyEntity();
        key.key = command.idempotencyKey();
        key.fingerprint = fingerprint;
        key.appointment = a;
        key.createdAt = Instant.now();
        keys.save(key);
        appointments.flush();
        keys.flush();
        return new AppointmentView(a.id, customer.id, vehicle.id, dealership.id, service.id, technician.id, bay.id,
                a.startTime, a.endTime, a.status, a.createdAt);
    }
}
