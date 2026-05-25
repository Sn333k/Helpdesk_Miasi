package helpdesk.domain.events;

import helpdesk.domain.domain.model.*;
import helpdesk.domain.model.RequesterID;
import helpdesk.domain.model.TicketID;

public class TicketCreated {

	private TicketID ticketId;
	private RequesterID requesterId;
	private Instant createdAt;

}