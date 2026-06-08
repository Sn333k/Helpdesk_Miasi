package com.miasi.helpdesk.infrastructure.inbound;

import com.miasi.helpdesk.application.domain.model.Ticket;
import com.miasi.helpdesk.application.ports.inbound.EscalateTicketUseCase;
import com.miasi.helpdesk.application.ports.outbound.ITicketRepository;
import java.time.Instant;
import java.util.List;

public class SlaCronJobAdapter {

  private final ITicketRepository ticketRepository;
  private final EscalateTicketUseCase escalateTicketUseCase;

  public SlaCronJobAdapter(
      ITicketRepository ticketRepository, EscalateTicketUseCase escalateTicketUseCase) {
    this.ticketRepository = ticketRepository;
    this.escalateTicketUseCase = escalateTicketUseCase;
  }

  public void checkSlaExceeded() {
    List<Ticket> breached = ticketRepository.findAllBreachingSla(Instant.now());
    System.out.printf(
        "[SLA-CHECK] %d breached ticket(s) found at %s%n", breached.size(), Instant.now());
    breached.forEach(t -> escalateTicketUseCase.escalate(t.getId()));
  }
}
