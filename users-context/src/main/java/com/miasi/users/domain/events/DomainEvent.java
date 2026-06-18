package com.miasi.users.domain.events;

import java.time.Instant;

public interface DomainEvent {
  Instant occurredAt();
}
