package helpdesk.application.ports.inbound;

import helpdesk.domain.model.TicketID;

public interface EscalateTicketUseCase {

	/**
	 * 
	 * @param ticketId
	 */
	void execute(TicketID ticketId);

}