package com.miasi.helpdesk.infrastructure.inbound;

import com.miasi.helpdesk.application.domain.model.Ticket;
import com.miasi.helpdesk.application.ports.inbound.EscalateTicketUseCase;
import com.miasi.helpdesk.application.ports.outbound.ITicketRepository;
import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

public class SlaCronJobAdapter {

  private static final Logger LOG = Logger.getLogger(SlaCronJobAdapter.class.getName());

  private final ITicketRepository ticketRepository;
  private final EscalateTicketUseCase escalateTicketUseCase;

  public SlaCronJobAdapter(
      ITicketRepository ticketRepository, EscalateTicketUseCase escalateTicketUseCase) {
    this.ticketRepository = ticketRepository;
    this.escalateTicketUseCase = escalateTicketUseCase;
  }

  public void checkSlaExceeded() {
    List<Ticket> breached = ticketRepository.findAllBreachingSla(Instant.now());
    LOG.info(() -> "SLA check: " + breached.size() + " breached ticket(s)");
    breached.forEach(t -> escalateTicketUseCase.escalate(t.getId()));
  }
}
