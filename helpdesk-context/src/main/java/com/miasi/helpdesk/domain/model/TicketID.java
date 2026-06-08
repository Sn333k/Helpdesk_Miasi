package com.miasi.helpdesk.domain.model;

public record TicketID(String id) {
  public TicketID {
    if (id == null || id.isBlank())
      throw new IllegalArgumentException("TicketID must not be blank");
  }
}
