package com.miasi.users.application.ports.outbound;

import com.miasi.users.domain.events.DomainEvent;

public interface IEventPublisher {
  void publish(DomainEvent event);
}
