package com.keyloop.scheduler.appointment.infrastructure;

import com.keyloop.scheduler.appointment.domain.AppointmentStatus;
import com.keyloop.scheduler.customer.infrastructure.CustomerEntity;
import com.keyloop.scheduler.vehicle.infrastructure.VehicleEntity;
import com.keyloop.scheduler.dealership.infrastructure.DealershipEntity;
import com.keyloop.scheduler.servicetype.infrastructure.ServiceTypeEntity;
import com.keyloop.scheduler.technician.infrastructure.TechnicianEntity;
import com.keyloop.scheduler.servicebay.infrastructure.ServiceBayEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "appointment")
public class AppointmentEntity {
    @Id
    public UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    public CustomerEntity customer;
    @Column(name = "vehicle_id", nullable = false)
    public UUID vehicleId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", insertable = false, updatable = false)
    public VehicleEntity vehicle;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dealership_id")
    public DealershipEntity dealership;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_type_id")
    public ServiceTypeEntity serviceType;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "technician_id")
    public TechnicianEntity technician;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_bay_id")
    public ServiceBayEntity serviceBay;
    @Column(name = "start_time", nullable = false)
    public Instant startTime;
    @Column(name = "end_time", nullable = false)
    public Instant endTime;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public AppointmentStatus status;
    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
    @Column(name = "cancelled_at")
    public Instant cancelledAt;

    protected AppointmentEntity() {
    }

    public AppointmentEntity(UUID id) {
        this.id = id;
    }
}
