package com.miasi.users.domain.events;

import com.miasi.users.domain.model.AccountStatus;
import com.miasi.users.domain.model.UserID;
import java.time.Instant;

public record UserDeactivated(UserID userId, AccountStatus newStatus, Instant occurredAt)
    implements DomainEvent {}
