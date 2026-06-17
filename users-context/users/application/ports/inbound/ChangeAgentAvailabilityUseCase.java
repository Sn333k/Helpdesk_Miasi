package com.miasi.users.application.ports.inbound;

import com.miasi.users.domain.model.AgentStatus;

/**
 * Inbound port: command to change an agent's availability.
 * Triggered via REST: PUT /api/users/{id}/availability
 */
public interface ChangeAgentAvailabilityUseCase {

    /**
     * @param agentId   the agent's UserID string
     * @param newStatus the target AgentStatus
     * @throws IllegalArgumentException if agentId is invalid or agent not found
     * @throws IllegalStateException    if the transition is not allowed
     */
    void changeAvailability(String agentId, AgentStatus newStatus);
}
