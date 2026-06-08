package helpdesk.application.domain.events;

import helpdesk.application.domain.domain.model.*;
import helpdesk.application.domain.model.AssigneeID;
import helpdesk.application.domain.model.TicketID;

public class TicketAssiged {

	private TicketID ticketId;
	private AssigneeID assigneeId;
	private Instant assignedAt;

}