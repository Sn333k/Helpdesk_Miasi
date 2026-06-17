package com.miasi.users.domain.model;

/**
 * Represents the availability status of an Assignee (agent).
 * State transitions:
 *   AVAILABLE -> BUSY -> AVAILABLE
 *   AVAILABLE -> UNAVAILABLE
 *   BUSY      -> UNAVAILABLE
 *   UNAVAILABLE -> AVAILABLE
 */
public enum AgentStatus {
    AVAILABLE,
    BUSY,
    UNAVAILABLE;

    public boolean canTakeNewTicket() {
        return this == AVAILABLE;
    }

    public AgentStatus transitionTo(AgentStatus newStatus) {
        if (this == newStatus) {
            throw new IllegalStateException(
                    "Agent is already in status: " + this);
        }
        return switch (this) {
            case AVAILABLE -> newStatus; // can go to BUSY or UNAVAILABLE
            case BUSY -> {
                if (newStatus == AVAILABLE || newStatus == UNAVAILABLE) yield newStatus;
                throw new IllegalStateException("Invalid transition from BUSY to " + newStatus);
            }
            case UNAVAILABLE -> {
                if (newStatus == AVAILABLE) yield newStatus;
                throw new IllegalStateException("Invalid transition from UNAVAILABLE to " + newStatus);
            }
        };
    }
}
