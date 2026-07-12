package com.keyloop.scheduler.api;

import com.keyloop.scheduler.support.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.assertj.core.api.Assertions.*;

@AutoConfigureMockMvc
class ConcurrentBookingApiIT extends PostgresIntegrationTest {
    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void seed() {
        SchedulingFixtures.reset(jdbc);
    }

    @Test
    void exactlyOneCompetingRequestWins() throws Exception {
        var body = "{\"customerId\":\"%s\",\"vehicleId\":\"%s\",\"dealershipId\":\"%s\",\"serviceTypeId\":\"%s\",\"startTime\":\"2030-01-01T10:00:00Z\"}".formatted(SchedulingFixtures.CUSTOMER, SchedulingFixtures.VEHICLE, SchedulingFixtures.DEALERSHIP, SchedulingFixtures.SERVICE);
        var gate = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var a = executor.submit(() -> request("race-key-001", body, gate));
            var b = executor.submit(() -> request("race-key-002", body, gate));
            gate.countDown();
            assertThat(java.util.List.of(a.get(), b.get())).containsExactlyInAnyOrder(201, 409);
        }
        assertThat(jdbc.queryForObject("select count(*) from appointment", Integer.class)).isOne();
        assertThat(jdbc.queryForObject("select count(*) from appointment a join appointment b on a.id<b.id and a.technician_id=b.technician_id and tstzrange(a.start_time,a.end_time,'[)') && tstzrange(b.start_time,b.end_time,'[)') and a.status='CONFIRMED' and b.status='CONFIRMED'", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from appointment a join appointment b on a.id<b.id and a.service_bay_id=b.service_bay_id and tstzrange(a.start_time,a.end_time,'[)') && tstzrange(b.start_time,b.end_time,'[)') and a.status='CONFIRMED' and b.status='CONFIRMED'", Integer.class)).isZero();
    }

    private int request(String key, String body, CountDownLatch gate) throws Exception {
        gate.await();
        return mvc.perform(post("/api/v1/appointments").header("Idempotency-Key", key).contentType("application/json").content(body)).andReturn().getResponse().getStatus();
    }
}
