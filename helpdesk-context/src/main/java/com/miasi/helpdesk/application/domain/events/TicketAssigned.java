package com.miasi.helpdesk.application.domain.events;

import com.miasi.helpdesk.application.domain.model.AssigneeID;
import com.miasi.helpdesk.application.domain.model.TicketID;
import java.time.Instant;

public record TicketAssigned(TicketID ticketId, AssigneeID assigneeId, Instant occurredAt)
    implements DomainEvent {}
