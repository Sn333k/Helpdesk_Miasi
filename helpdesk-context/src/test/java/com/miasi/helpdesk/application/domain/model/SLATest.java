package com.miasi.helpdesk.application.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class SLATest {

  @Test
  void isBreached_whenNowAfterDeadline_returnsTrue() {
    SLA sla = new SLA(Instant.now().minusSeconds(1));

    assertThat(sla.isBreached(Instant.now())).isTrue();
  }

  @Test
  void isBreached_whenNowBeforeDeadline_returnsFalse() {
    SLA sla = new SLA(Instant.now().plusSeconds(3600));

    assertThat(sla.isBreached(Instant.now())).isFalse();
  }

  @Test
  void isBreached_atExactDeadline_returnsFalse() {
    Instant deadline = Instant.now().plusSeconds(60);
    SLA sla = new SLA(deadline);

    // isAfter is strict – at exactly the deadline moment it is not yet breached
    assertThat(sla.isBreached(deadline)).isFalse();
  }

  @Test
  void nullDeadline_shouldThrow() {
    assertThatThrownBy(() -> new SLA(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("deadline");
  }
}
