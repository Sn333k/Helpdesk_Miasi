package com.miasi.users.domain.model;

public enum AgentStatus {
  AVAILABLE,
  BUSY,
  OFFLINE;

  public boolean isAvailable() {
    return this == AVAILABLE;
  }
}
