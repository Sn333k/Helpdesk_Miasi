package com.miasi.helpdesk.application.domain.events;

import java.time.Instant;

public interface DomainEvent {
  Instant occurredAt();
}
