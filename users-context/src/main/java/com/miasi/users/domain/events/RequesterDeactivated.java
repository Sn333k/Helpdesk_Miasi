package com.miasi.users.domain.events;

import com.miasi.users.domain.model.UserID;
import java.time.Instant;

public record RequesterDeactivated(UserID requesterId, Instant occurredAt) implements DomainEvent {}
