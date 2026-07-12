package com.keyloop.scheduler.servicetype.infrastructure;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "service_type")
public class ServiceTypeEntity {
    @Id
    public UUID id;
    @Column(nullable = false, unique = true)
    public String code;
    @Column(nullable = false)
    public String name;
    @Column(name = "duration_minutes", nullable = false)
    public int durationMinutes;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "required_qualification_id")
    public QualificationEntity requiredQualification;

    protected ServiceTypeEntity() {
    }
}
