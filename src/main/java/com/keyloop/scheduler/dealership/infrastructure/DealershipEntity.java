package com.keyloop.scheduler.dealership.infrastructure;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "dealership")
public class DealershipEntity {
    @Id
    public UUID id;
    @Column(nullable = false, unique = true)
    public String code;
    @Column(nullable = false)
    public String name;
    @Column(nullable = false)
    public boolean active;

    protected DealershipEntity() {
    }
}
