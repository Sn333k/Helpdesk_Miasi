package helpdesk.application.domain.events;

import helpdesk.application.domain.domain.model.*;
import helpdesk.application.domain.model.TicketID;
import helpdesk.application.domain.model.TicketStatus;

public class TicketStatusChanged {

	private TicketID ticketId;
	private TicketStatus oldStatus;
	private TicketStatus newStatus;

}