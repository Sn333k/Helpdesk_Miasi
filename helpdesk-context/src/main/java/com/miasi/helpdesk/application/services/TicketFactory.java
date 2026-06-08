package com.miasi.helpdesk.application.services;

import com.miasi.helpdesk.application.domain.model.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TicketFactory {

  private final long slaMinutes;

  public TicketFactory(long slaMinutes) {
    this.slaMinutes = slaMinutes;
  }

  public Ticket create(
      String title, String description, RequesterID requesterId, Category category) {
    TicketPriority initialPriority = TicketPriority.MEDIUM;
    SLA sla = new SLA(Instant.now().plus(slaMinutes, ChronoUnit.MINUTES));
    return new Ticket(title, description, requesterId, category, sla, initialPriority);
  }
}
