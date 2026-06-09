package com.miasi.helpdesk.application.ports.outbound;

import com.miasi.helpdesk.application.domain.events.DomainEvent;

public interface INotificationSender {
  void sendNotification(DomainEvent event);
}
