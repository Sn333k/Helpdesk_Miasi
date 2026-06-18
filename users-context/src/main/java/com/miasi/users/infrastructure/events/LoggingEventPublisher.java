package com.miasi.users.infrastructure.events;

import com.miasi.users.application.ports.outbound.IEventPublisher;
import com.miasi.users.domain.events.DomainEvent;
import java.util.logging.Logger;

public class LoggingEventPublisher implements IEventPublisher {

  private static final Logger LOG = Logger.getLogger(LoggingEventPublisher.class.getName());

  @Override
  public void publish(DomainEvent event) {
    LOG.info(() -> "[EVENT] " + event.getClass().getSimpleName() + " at " + event.occurredAt());
  }
}
