package com.miasi.helpdesk.domain.events;

import com.miasi.helpdesk.domain.model.TicketID;
import java.time.Instant;

public record SLABreached(TicketID ticketId, Instant occurredAt) implements DomainEvent {}
