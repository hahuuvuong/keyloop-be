package com.keyloop.scheduler.appointment.infrastructure;

import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID>, JpaSpecificationExecutor<AppointmentEntity> {
    @EntityGraph(attributePaths = {"customer", "vehicle", "dealership", "serviceType", "technician", "serviceBay"})
    @Query("select a from AppointmentEntity a where a.id=:id")
    Optional<AppointmentEntity> findDetailedById(@org.springframework.data.repository.query.Param("id") UUID id);
}
