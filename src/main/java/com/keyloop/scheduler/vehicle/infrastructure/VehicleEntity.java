package com.keyloop.scheduler.vehicle.infrastructure;

import com.keyloop.scheduler.customer.infrastructure.CustomerEntity;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "vehicle")
public class VehicleEntity {
    @Id
    public UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    public CustomerEntity customer;
    @Column(nullable = false, unique = true)
    public String registration;
    public String make;
    public String model;

    protected VehicleEntity() {
    }
}
