package com.keyloop.scheduler.servicetype.infrastructure;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "qualification")
public class QualificationEntity {
    @Id
    public UUID id;
    @Column(nullable = false, unique = true)
    public String code;
    @Column(nullable = false)
    public String name;

    protected QualificationEntity() {
    }
}
