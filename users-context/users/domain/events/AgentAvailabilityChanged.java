package com.miasi.users.domain.events;

import com.miasi.users.domain.model.AgentStatus;
import com.miasi.users.domain.model.UserID;

/**
 * Published when an agent's availability status changes.
 * Consumed by the Ticket context: if an agent becomes UNAVAILABLE,
 * any assigned ticket is reverted to NEW and re-queued.
 */
public final class AgentAvailabilityChanged extends DomainEvent {

    private final UserID agentId;
    private final AgentStatus newStatus;

    public AgentAvailabilityChanged(UserID agentId, AgentStatus newStatus) {
        super();
        if (agentId == null) throw new IllegalArgumentException("agentId must not be null");
        if (newStatus == null) throw new IllegalArgumentException("newStatus must not be null");
        this.agentId = agentId;
        this.newStatus = newStatus;
    }

    public UserID getAgentId() {
        return agentId;
    }

    public AgentStatus getNewStatus() {
        return newStatus;
    }

    @Override
    public String toString() {
        return "AgentAvailabilityChanged{agentId=" + agentId
                + ", newStatus=" + newStatus
                + ", occurredAt=" + getOccurredAt() + "}";
    }
}
