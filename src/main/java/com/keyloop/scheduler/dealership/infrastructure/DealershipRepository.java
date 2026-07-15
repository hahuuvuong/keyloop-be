package com.keyloop.scheduler.dealership.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DealershipRepository extends JpaRepository<DealershipEntity, UUID> {
}
