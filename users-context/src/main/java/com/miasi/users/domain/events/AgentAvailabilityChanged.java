package com.miasi.users.domain.events;

import com.miasi.users.domain.model.AgentStatus;
import com.miasi.users.domain.model.UserID;
import java.time.Instant;

public record AgentAvailabilityChanged(UserID userId, AgentStatus newStatus, Instant occurredAt)
    implements DomainEvent {}
