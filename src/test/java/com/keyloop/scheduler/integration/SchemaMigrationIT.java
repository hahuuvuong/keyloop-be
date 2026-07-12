package com.keyloop.scheduler.integration;

import com.keyloop.scheduler.support.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.*;

class SchemaMigrationIT extends PostgresIntegrationTest {
    @Autowired
    JdbcTemplate jdbc;

    @Test
    void extensionAndConstraintsMatchInvariant() {
        assertThat(jdbc.queryForObject("select count(*) from pg_extension where extname='btree_gist'", Integer.class)).isOne();
        var definitions = jdbc.queryForList("select pg_get_constraintdef(oid) definition from pg_constraint where conname in ('ex_appointment_technician_overlap','ex_appointment_bay_overlap')", String.class);
        assertThat(definitions).hasSize(2).allSatisfy(d -> {
            assertThat(d).contains("tstzrange").contains("[)").contains("&&").contains("CONFIRMED");
        });
    }
}
