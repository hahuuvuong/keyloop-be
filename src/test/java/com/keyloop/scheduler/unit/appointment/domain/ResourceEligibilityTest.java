package com.keyloop.scheduler.unit.appointment.domain;
import org.junit.jupiter.api.Test; import java.util.*; import static org.assertj.core.api.Assertions.*;
class ResourceEligibilityTest {@Test void stableUuidOrderingIsDeterministic(){var first=UUID.fromString("00000000-0000-0000-0000-000000000001");var second=UUID.fromString("00000000-0000-0000-0000-000000000002");var candidates=new ArrayList<>(List.of(second,first));candidates.sort(Comparator.naturalOrder());assertThat(candidates).containsExactly(first,second);}}
