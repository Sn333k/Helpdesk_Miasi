package helpdesk.application.ports.inbound;

import helpdesk.domain.model.RequesterID;
import helpdesk.domain.model.TicketID;

public interface CreateTicketUseCase {

	/**
	 * 
	 * @param title
	 * @param description
	 * @param requester
	 */
	TicketID execute(String title, String description, RequesterID requester);

}