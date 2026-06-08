package com.miasi.helpdesk.application.domain.model;

public enum TicketPriority {
  LOW(1),
  MEDIUM(2),
  HIGH(3),
  CRITICAL(4);

  private final int level;

  TicketPriority(int level) {
    this.level = level;
  }

  public int level() {
    return level;
  }

  public TicketPriority escalate() {
    return switch (this) {
      case LOW -> MEDIUM;
      case MEDIUM -> HIGH;
      case HIGH -> CRITICAL;
      case CRITICAL -> CRITICAL;
    };
  }

  public boolean isCritical() {
    return this == CRITICAL;
  }
}
