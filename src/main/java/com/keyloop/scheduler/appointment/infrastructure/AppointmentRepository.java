package com.keyloop.scheduler.appointment.infrastructure;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID>, JpaSpecificationExecutor<AppointmentEntity> {
    @Override
    @EntityGraph(attributePaths = {"customer", "vehicle", "dealership", "serviceType", "technician", "serviceBay"})
    Page<AppointmentEntity> findAll(Specification<AppointmentEntity> specification, Pageable pageable);

    @EntityGraph(attributePaths = {"customer", "vehicle", "dealership", "serviceType", "technician", "serviceBay"})
    @Query("select a from AppointmentEntity a where a.id=:id")
    Optional<AppointmentEntity> findDetailedById(@org.springframework.data.repository.query.Param("id") UUID id);
}
