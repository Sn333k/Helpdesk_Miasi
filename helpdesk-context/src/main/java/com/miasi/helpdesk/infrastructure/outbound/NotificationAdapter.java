package com.miasi.helpdesk.infrastructure.outbound;

import com.miasi.helpdesk.application.domain.events.DomainEvent;
import com.miasi.helpdesk.application.ports.outbound.INotificationSender;
import java.util.logging.Logger;

public class NotificationAdapter implements INotificationSender {

  private static final Logger LOG = Logger.getLogger(NotificationAdapter.class.getName());

  @Override
  public void sendNotification(DomainEvent event) {
    LOG.info(
        () -> "[NOTIFICATION] " + event.getClass().getSimpleName() + " at " + event.occurredAt());
  }
}
