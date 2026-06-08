package helpdesk.application.ports.inbound;

import helpdesk.application.domain.model.RequesterID;
import helpdesk.application.domain.model.TicketID;

public interface CreateTicketUseCase {

	/**
	 * 
	 * @param title
	 * @param description
	 * @param requester
	 */
	TicketID execute(String title, String description, RequesterID requester);

}