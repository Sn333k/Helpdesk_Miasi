package helpdesk.application.services;

import helpdesk.application.application.ports.inbound.*;
import helpdesk.application.ports.inbound.AssignTicketUseCase;
import helpdesk.application.ports.inbound.CreateTicketUseCase;
import helpdesk.application.ports.inbound.EscalateTicketUseCase;
import helpdesk.application.domain.model.RequesterID;
import helpdesk.application.domain.model.TicketID;
import users.domain.services.*;

public class TicketApplicationService implements AssignTicketUseCase, EscalateTicketUseCase, CreateTicketUseCase {

	private StaffAssignmentService domainAssignmentService;

	/**
	 * 
	 * @param title
	 * @param description
	 * @param requester
	 */
	public TicketID execute(String title, String description, RequesterID requester) {
		// TODO - implement TicketApplicationService.execute
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param ticketId
	 */
	public void execute(TicketID ticketId) {
		// TODO - implement TicketApplicationService.execute
		throw new UnsupportedOperationException();
	}

}