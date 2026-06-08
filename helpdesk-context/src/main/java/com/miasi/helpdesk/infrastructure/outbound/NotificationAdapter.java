package com.miasi.helpdesk.infrastructure.outbound;

import com.miasi.helpdesk.application.ports.outbound.INotificationSender;
import com.miasi.helpdesk.domain.events.DomainEvent;

public class NotificationAdapter implements INotificationSender {

  @Override
  public void sendNotification(DomainEvent event) {
    System.out.printf(
        "[NOTIFICATION] %s at %s%n", event.getClass().getSimpleName(), event.occurredAt());
  }
}
