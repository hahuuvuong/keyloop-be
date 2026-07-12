package com.keyloop.scheduler.vehicle.infrastructure;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface VehicleRepository extends JpaRepository<VehicleEntity, UUID> {
    boolean existsByIdAndCustomerId(UUID id, UUID customerId);
}
