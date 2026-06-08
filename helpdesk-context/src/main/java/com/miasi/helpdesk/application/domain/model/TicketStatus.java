package com.miasi.helpdesk.application.domain.model;

import java.util.Map;
import java.util.Set;

public enum TicketStatus {
  NEW,
  ASSIGNED,
  IN_PROGRESS,
  RESOLVED,
  CLOSED;

  private static final Map<TicketStatus, Set<TicketStatus>> TRANSITIONS =
      Map.of(
          NEW, Set.of(ASSIGNED, CLOSED),
          ASSIGNED, Set.of(IN_PROGRESS, CLOSED),
          IN_PROGRESS, Set.of(RESOLVED, CLOSED),
          RESOLVED, Set.of(CLOSED),
          CLOSED, Set.of());

  public boolean canTransitionTo(TicketStatus next) {
    return TRANSITIONS.getOrDefault(this, Set.of()).contains(next);
  }
}
