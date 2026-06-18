package com.miasi.users.domain.model;

public enum AgentStatus {
  AVAILABLE,
  BUSY,
  UNAVAILABLE;

  public boolean isAvailable() {
    return this == AVAILABLE;
  }
}
