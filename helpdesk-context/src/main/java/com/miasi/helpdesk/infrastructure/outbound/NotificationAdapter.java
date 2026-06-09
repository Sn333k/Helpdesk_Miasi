package com.miasi.helpdesk.infrastructure.outbound;

import com.miasi.helpdesk.application.domain.events.DomainEvent;
import com.miasi.helpdesk.application.ports.outbound.INotificationSender;

public class NotificationAdapter implements INotificationSender {

  @Override
  public void sendNotification(DomainEvent event) {
    System.out.printf(
        "[NOTIFICATION] %s at %s%n", event.getClass().getSimpleName(), event.occurredAt());
  }
}
