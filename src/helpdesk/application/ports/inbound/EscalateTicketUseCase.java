package helpdesk.application.ports.inbound;

import helpdesk.application.domain.model.TicketID;

public interface EscalateTicketUseCase {

	/**
	 * 
	 * @param ticketId
	 */
	void execute(TicketID ticketId);

}