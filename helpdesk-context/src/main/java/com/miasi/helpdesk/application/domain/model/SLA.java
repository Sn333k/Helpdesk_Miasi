package com.miasi.helpdesk.application.domain.model;

import java.time.Instant;

public record SLA(Instant deadline) {
  public SLA {
    if (deadline == null) throw new IllegalArgumentException("SLA deadline must not be null");
  }

  public boolean isBreached(Instant now) {
    return now.isAfter(deadline);
  }
}
