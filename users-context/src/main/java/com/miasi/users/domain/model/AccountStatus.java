package com.miasi.users.domain.model;

public enum AccountStatus {
  ACTIVE,
  INACTIVE,
  SUSPENDED;

  public boolean isActive() {
    return this == ACTIVE;
  }
}
