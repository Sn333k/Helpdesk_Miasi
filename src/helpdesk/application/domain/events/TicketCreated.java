package helpdesk.application.domain.events;

import helpdesk.application.domain.domain.model.*;
import helpdesk.application.domain.model.RequesterID;
import helpdesk.application.domain.model.TicketID;

public class TicketCreated {

	private TicketID ticketId;
	private RequesterID requesterId;
	private Instant createdAt;

}