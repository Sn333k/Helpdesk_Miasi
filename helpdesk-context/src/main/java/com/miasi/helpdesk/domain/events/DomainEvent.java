package com.miasi.helpdesk.domain.events;

import java.time.Instant;

public interface DomainEvent {
  Instant occurredAt();
}
