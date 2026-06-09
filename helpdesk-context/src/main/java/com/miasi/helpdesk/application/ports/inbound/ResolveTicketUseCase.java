package com.miasi.helpdesk.application.ports.inbound;

import com.miasi.helpdesk.application.domain.model.TicketID;

public interface ResolveTicketUseCase {
  void execute(TicketID ticketId, String resolution);
}
