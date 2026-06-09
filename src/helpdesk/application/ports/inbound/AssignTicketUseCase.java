package helpdesk.application.ports.inbound;

import helpdesk.application.domain.model.TicketID;

public interface AssignTicketUseCase {

	/**
	 * 
	 * @param ticketId
	 */
	void execute(TicketID ticketId);

}