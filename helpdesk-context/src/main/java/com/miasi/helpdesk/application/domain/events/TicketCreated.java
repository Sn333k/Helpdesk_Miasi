package com.miasi.helpdesk.application.domain.events;

import com.miasi.helpdesk.application.domain.model.RequesterID;
import com.miasi.helpdesk.application.domain.model.TicketID;
import java.time.Instant;

public record TicketCreated(TicketID ticketId, RequesterID requesterId, Instant occurredAt)
    implements DomainEvent {}
