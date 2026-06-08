package com.miasi.helpdesk.domain.events;

import com.miasi.helpdesk.domain.model.AssigneeID;
import com.miasi.helpdesk.domain.model.TicketID;
import java.time.Instant;

public record TicketAssigned(TicketID ticketId, AssigneeID assigneeId, Instant occurredAt)
    implements DomainEvent {}
