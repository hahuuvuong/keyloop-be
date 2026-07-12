package com.keyloop.scheduler.servicebay.infrastructure;

import com.keyloop.scheduler.dealership.infrastructure.DealershipEntity;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "service_bay")
public class ServiceBayEntity {
    @Id
    public UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dealership_id")
    public DealershipEntity dealership;
    @Column(nullable = false)
    public String code;
    @Column(nullable = false)
    public boolean active;

    protected ServiceBayEntity() {
    }
}
