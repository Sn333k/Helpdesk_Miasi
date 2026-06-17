package com.miasi.users.domain.events;

import java.time.Instant;

/**
 * Base class for all domain events in the Users bounded context.
 */
public abstract class DomainEvent {

    private final Instant occurredAt;

    protected DomainEvent() {
        this.occurredAt = Instant.now();
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
