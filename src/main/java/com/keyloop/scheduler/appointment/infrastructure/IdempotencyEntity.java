package com.keyloop.scheduler.appointment.infrastructure;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "appointment_idempotency")
public class IdempotencyEntity {
    @Id
    @Column(name = "idempotency_key")
    public String key;
    @Column(name = "request_fingerprint", nullable = false, columnDefinition = "char(64)")
    @JdbcTypeCode(SqlTypes.CHAR)
    public String fingerprint;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", unique = true)
    public AppointmentEntity appointment;
    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    public IdempotencyEntity() {
    }
}
