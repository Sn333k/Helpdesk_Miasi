package com.miasi.users.domain.events;

import com.miasi.users.domain.model.UserID;
import java.time.Instant;

public record AssigneeDeactivated(UserID assigneeId, Instant occurredAt) implements DomainEvent {}
