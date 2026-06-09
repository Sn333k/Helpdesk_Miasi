package helpdesk.application.domain.events;

import helpdesk.application.domain.domain.model.*;
import helpdesk.application.domain.model.TicketID;

public class SLABreached {

	private TicketID ticketId;
	private Instant breachedAt;

}