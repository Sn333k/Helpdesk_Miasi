package com.miasi.users.domain.events;

import com.miasi.users.domain.model.SupportTeamID;
import com.miasi.users.domain.model.UserID;
import java.time.Instant;

public record AgentAssignedToTeam(UserID agentId, SupportTeamID teamId, Instant occurredAt)
    implements DomainEvent {}
