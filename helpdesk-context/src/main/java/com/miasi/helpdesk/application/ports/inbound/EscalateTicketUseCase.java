package com.miasi.helpdesk.application.ports.inbound;

import com.miasi.helpdesk.application.domain.model.TicketID;

public interface EscalateTicketUseCase {
  void escalate(TicketID ticketId);
}
