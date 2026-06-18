package com.miasi.helpdesk.application.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TicketPriorityTest {

  @Test
  void escalateLow_returnsMedium() {
    assertThat(TicketPriority.LOW.escalate()).isEqualTo(TicketPriority.MEDIUM);
  }

  @Test
  void escalateMedium_returnsHigh() {
    assertThat(TicketPriority.MEDIUM.escalate()).isEqualTo(TicketPriority.HIGH);
  }

  @Test
  void escalateHigh_returnsCritical() {
    assertThat(TicketPriority.HIGH.escalate()).isEqualTo(TicketPriority.CRITICAL);
  }

  @Test
  void escalateCritical_remainsCritical() {
    assertThat(TicketPriority.CRITICAL.escalate()).isEqualTo(TicketPriority.CRITICAL);
  }

  @Test
  void isCritical_onlyForCritical() {
    assertThat(TicketPriority.CRITICAL.isCritical()).isTrue();
    assertThat(TicketPriority.HIGH.isCritical()).isFalse();
    assertThat(TicketPriority.MEDIUM.isCritical()).isFalse();
    assertThat(TicketPriority.LOW.isCritical()).isFalse();
  }

  @Test
  void levelValues_areStrictlyIncreasing() {
    assertThat(TicketPriority.LOW.level()).isLessThan(TicketPriority.MEDIUM.level());
    assertThat(TicketPriority.MEDIUM.level()).isLessThan(TicketPriority.HIGH.level());
    assertThat(TicketPriority.HIGH.level()).isLessThan(TicketPriority.CRITICAL.level());
  }
}
