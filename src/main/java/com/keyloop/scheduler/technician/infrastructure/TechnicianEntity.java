package com.keyloop.scheduler.technician.infrastructure;

import com.keyloop.scheduler.dealership.infrastructure.DealershipEntity;
import com.keyloop.scheduler.servicetype.infrastructure.QualificationEntity;
import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "technician")
public class TechnicianEntity {
    @Id
    public UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dealership_id")
    public DealershipEntity dealership;
    @Column(name = "employee_reference", nullable = false)
    public String employeeReference;
    @Column(name = "display_name", nullable = false)
    public String displayName;
    @Column(nullable = false)
    public boolean active;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "technician_qualification", joinColumns = @JoinColumn(name = "technician_id"), inverseJoinColumns = @JoinColumn(name = "qualification_id"))
    public Set<QualificationEntity> qualifications = new HashSet<>();

    protected TechnicianEntity() {
    }
}
