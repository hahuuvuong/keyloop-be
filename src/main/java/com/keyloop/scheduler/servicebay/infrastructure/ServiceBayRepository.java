package com.keyloop.scheduler.servicebay.infrastructure;
import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.time.Instant; import java.util.*;
public interface ServiceBayRepository extends JpaRepository<ServiceBayEntity, UUID> {
 @Query("select b from ServiceBayEntity b where b.dealership.id=:dealership and b.active=true and not exists (select a.id from AppointmentEntity a where a.serviceBay.id=b.id and a.status=com.keyloop.scheduler.appointment.domain.AppointmentStatus.CONFIRMED and a.startTime<:end and a.endTime>:start) order by b.id")
 List<ServiceBayEntity> findEligible(@Param("dealership") UUID dealership,@Param("start") Instant start,@Param("end") Instant end);
}
