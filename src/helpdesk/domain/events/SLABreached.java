package helpdesk.domain.events;

import helpdesk.domain.domain.model.*;
import helpdesk.domain.model.TicketID;

public class SLABreached {

	private TicketID ticketId;
	private Instant breachedAt;

}