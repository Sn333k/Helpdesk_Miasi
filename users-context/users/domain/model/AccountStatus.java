package com.miasi.users.domain.model;

/**
 * Represents the account lifecycle status of any user (Assignee or Requester).
 * State transitions: ACTIVE <-> SUSPENDED -> (terminal)
 */
public enum AccountStatus {
    ACTIVE,
    SUSPENDED;

    public AccountStatus activate() {
        if (this == SUSPENDED) return ACTIVE;
        throw new IllegalStateException("Cannot activate an account that is already ACTIVE");
    }

    public AccountStatus suspend() {
        if (this == ACTIVE) return SUSPENDED;
        throw new IllegalStateException("Cannot suspend an account that is already SUSPENDED");
    }

    public boolean isActive() {
        return this == ACTIVE;
    }
}
