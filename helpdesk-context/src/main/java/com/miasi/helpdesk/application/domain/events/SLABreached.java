package com.miasi.helpdesk.application.domain.events;

import com.miasi.helpdesk.application.domain.model.TicketID;
import java.time.Instant;

public record SLABreached(TicketID ticketId, Instant occurredAt) implements DomainEvent {}
