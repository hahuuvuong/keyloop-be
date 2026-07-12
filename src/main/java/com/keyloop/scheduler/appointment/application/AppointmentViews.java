package com.keyloop.scheduler.appointment.application;

import com.keyloop.scheduler.appointment.infrastructure.AppointmentEntity;

public final class AppointmentViews {
    private AppointmentViews() {
    }

    public static AppointmentView from(AppointmentEntity a) {
        return new AppointmentView(a.id, a.customer.id, a.vehicle.id, a.dealership.id, a.serviceType.id, a.technician.id, a.serviceBay.id, a.startTime, a.endTime, a.status, a.createdAt);
    }
}
