package com.keyloop.scheduler.unit.appointment.domain;
import com.keyloop.scheduler.appointment.domain.AppointmentInterval; import org.junit.jupiter.api.Test; import java.time.Instant; import static org.assertj.core.api.Assertions.*;
class AppointmentIntervalTest {private final Instant ten=Instant.parse("2030-01-01T10:00:00Z");
 @Test void calculatesEndTime(){assertThat(AppointmentInterval.startingAt(ten,60).end()).isEqualTo(Instant.parse("2030-01-01T11:00:00Z"));}
 @Test void overlappingIntervalsConflict(){assertThat(new AppointmentInterval(ten,ten.plusSeconds(3600)).overlaps(new AppointmentInterval(ten.plusSeconds(1),ten.plusSeconds(7200)))).isTrue();}
 @Test void backToBackIntervalsDoNotOverlap(){assertThat(new AppointmentInterval(ten,ten.plusSeconds(3600)).overlaps(new AppointmentInterval(ten.plusSeconds(3600),ten.plusSeconds(7200)))).isFalse();}
 @Test void rejectsInvalidDuration(){assertThatThrownBy(()->AppointmentInterval.startingAt(ten,0)).isInstanceOf(IllegalArgumentException.class);}
}
