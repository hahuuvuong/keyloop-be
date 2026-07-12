package com.keyloop.scheduler.appointment.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IdempotencyRepository extends JpaRepository<IdempotencyEntity, String> {
    @EntityGraph(attributePaths = {"appointment", "appointment.customer", "appointment.vehicle",
            "appointment.dealership", "appointment.serviceType", "appointment.technician", "appointment.serviceBay"})
    @Query("select i from IdempotencyEntity i where i.key=:key")
    Optional<IdempotencyEntity> findDetailedByKey(@Param("key") String key);
}
