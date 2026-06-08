package com.miasi.helpdesk.domain.events;

import com.miasi.helpdesk.domain.model.TicketID;
import com.miasi.helpdesk.domain.model.TicketStatus;
import java.time.Instant;

public record TicketStatusChanged(
    TicketID ticketId, TicketStatus oldStatus, TicketStatus newStatus, Instant occurredAt)
    implements DomainEvent {}
