package com.miasi.helpdesk.domain.events;

import com.miasi.helpdesk.domain.model.RequesterID;
import com.miasi.helpdesk.domain.model.TicketID;
import java.time.Instant;

public record TicketCreated(TicketID ticketId, RequesterID requesterId, Instant occurredAt)
    implements DomainEvent {}
