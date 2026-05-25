package helpdesk.application.ports.inbound;

import helpdesk.domain.model.TicketID;

public interface AssignTicketUseCase {

	/**
	 * 
	 * @param ticketId
	 */
	void execute(TicketID ticketId);

}