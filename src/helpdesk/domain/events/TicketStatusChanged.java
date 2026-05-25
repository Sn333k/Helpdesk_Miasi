package helpdesk.domain.events;

import helpdesk.domain.domain.model.*;
import helpdesk.domain.model.TicketID;
import helpdesk.domain.model.TicketStatus;

public class TicketStatusChanged {

	private TicketID ticketId;
	private TicketStatus oldStatus;
	private TicketStatus newStatus;

}