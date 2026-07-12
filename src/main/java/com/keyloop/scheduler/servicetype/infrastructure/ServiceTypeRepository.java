package com.keyloop.scheduler.servicetype.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ServiceTypeRepository extends JpaRepository<ServiceTypeEntity, UUID> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "requiredQualification")
    java.util.Optional<ServiceTypeEntity> findById(UUID id);
}
