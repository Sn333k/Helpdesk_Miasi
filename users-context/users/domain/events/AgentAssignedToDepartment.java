package com.miasi.users.domain.events;

import com.miasi.users.domain.model.SupportTeamID;
import com.miasi.users.domain.model.UserID;

/**
 * Published when an agent is assigned to a SupportTeam (department).
 */
public final class AgentAssignedToDepartment extends DomainEvent {

    private final UserID agentId;
    private final SupportTeamID departmentId;

    public AgentAssignedToDepartment(UserID agentId, SupportTeamID departmentId) {
        super();
        if (agentId == null) throw new IllegalArgumentException("agentId must not be null");
        if (departmentId == null) throw new IllegalArgumentException("departmentId must not be null");
        this.agentId = agentId;
        this.departmentId = departmentId;
    }

    public UserID getAgentId() {
        return agentId;
    }

    public SupportTeamID getDepartmentId() {
        return departmentId;
    }

    @Override
    public String toString() {
        return "AgentAssignedToDepartment{agentId=" + agentId
                + ", departmentId=" + departmentId
                + ", occurredAt=" + getOccurredAt() + "}";
    }
}
