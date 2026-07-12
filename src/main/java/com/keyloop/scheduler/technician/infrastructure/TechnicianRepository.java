package com.keyloop.scheduler.technician.infrastructure;
import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.time.Instant; import java.util.*;
public interface TechnicianRepository extends JpaRepository<TechnicianEntity, UUID> {
 @Query("select distinct t from TechnicianEntity t join t.qualifications q where t.dealership.id=:dealership and t.active=true and q.id=:qualification and not exists (select a.id from AppointmentEntity a where a.technician.id=t.id and a.status=com.keyloop.scheduler.appointment.domain.AppointmentStatus.CONFIRMED and a.startTime<:end and a.endTime>:start) order by t.id")
 List<TechnicianEntity> findEligible(@Param("dealership") UUID dealership,@Param("qualification") UUID qualification,@Param("start") Instant start,@Param("end") Instant end);
}
