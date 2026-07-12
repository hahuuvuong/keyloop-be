package com.keyloop.scheduler.customer.infrastructure;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "customer")
public class CustomerEntity {
    @Id
    public UUID id;
    @Column(nullable = false, unique = true)
    public String reference;
    @Column(name = "display_name", nullable = false)
    public String displayName;

    protected CustomerEntity() {
    }
}
