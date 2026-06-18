package com.miasi.helpdesk.application.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TicketStatusTest {

  @Test
  void newCanTransitionToAssignedAndClosed() {
    assertThat(TicketStatus.NEW.canTransitionTo(TicketStatus.ASSIGNED)).isTrue();
    assertThat(TicketStatus.NEW.canTransitionTo(TicketStatus.CLOSED)).isTrue();
  }

  @Test
  void newCannotTransitionToResolved() {
    assertThat(TicketStatus.NEW.canTransitionTo(TicketStatus.RESOLVED)).isFalse();
  }

  @Test
  void newCannotTransitionToInProgress() {
    assertThat(TicketStatus.NEW.canTransitionTo(TicketStatus.IN_PROGRESS)).isFalse();
  }

  @Test
  void assignedCanTransitionToInProgressAndClosed() {
    assertThat(TicketStatus.ASSIGNED.canTransitionTo(TicketStatus.IN_PROGRESS)).isTrue();
    assertThat(TicketStatus.ASSIGNED.canTransitionTo(TicketStatus.CLOSED)).isTrue();
  }

  @Test
  void assignedCannotTransitionToResolved() {
    assertThat(TicketStatus.ASSIGNED.canTransitionTo(TicketStatus.RESOLVED)).isFalse();
  }

  @Test
  void inProgressCanTransitionToResolvedAndClosed() {
    assertThat(TicketStatus.IN_PROGRESS.canTransitionTo(TicketStatus.RESOLVED)).isTrue();
    assertThat(TicketStatus.IN_PROGRESS.canTransitionTo(TicketStatus.CLOSED)).isTrue();
  }

  @Test
  void resolvedCanOnlyTransitionToClosed() {
    assertThat(TicketStatus.RESOLVED.canTransitionTo(TicketStatus.CLOSED)).isTrue();
    assertThat(TicketStatus.RESOLVED.canTransitionTo(TicketStatus.ASSIGNED)).isFalse();
    assertThat(TicketStatus.RESOLVED.canTransitionTo(TicketStatus.IN_PROGRESS)).isFalse();
    assertThat(TicketStatus.RESOLVED.canTransitionTo(TicketStatus.NEW)).isFalse();
  }

  @Test
  void closedHasNoValidTransitions() {
    for (TicketStatus target : TicketStatus.values()) {
      assertThat(TicketStatus.CLOSED.canTransitionTo(target)).isFalse();
    }
  }
}
