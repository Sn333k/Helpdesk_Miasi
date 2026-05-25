package helpdesk.domain.events;

import helpdesk.domain.domain.model.*;
import helpdesk.domain.model.AssigneeID;
import helpdesk.domain.model.TicketID;

public class TicketAssiged {

	private TicketID ticketId;
	private AssigneeID assigneeId;
	private Instant assignedAt;

}