package com.miasi.helpdesk.application.ports.inbound;

import com.miasi.helpdesk.application.domain.model.TicketID;

public interface CreateTicketUseCase {
  TicketID execute(String title, String description, String requesterId);
}
