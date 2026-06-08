package com.miasi.helpdesk.application.ports.outbound;

import com.miasi.helpdesk.domain.model.Ticket;
import com.miasi.helpdesk.domain.model.TicketID;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ITicketRepository {
  void save(Ticket ticket);

  Optional<Ticket> findById(TicketID id);

  List<Ticket> findAllBreachingSla(Instant now);
}
