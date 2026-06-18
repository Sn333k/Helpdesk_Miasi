package com.miasi.helpdesk.application.domain.model;

public class TicketNotFoundException extends RuntimeException {
  public TicketNotFoundException(TicketID id) {
    super("Ticket not found: " + id.id());
  }
}
