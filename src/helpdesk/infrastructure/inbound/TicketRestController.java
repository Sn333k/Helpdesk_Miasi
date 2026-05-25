package helpdesk.infrastructure.inbound;

import helpdesk.application.ports.inbound.CreateTicketUseCase;

public class TicketRestController {

	private CreateTicketUseCase createTicketUseCase;

	/**
	 * 
	 * @param createTicketUseCase
	 */
	public TicketRestController(CreateTicketUseCase createTicketUseCase) {
		// TODO - implement TicketRestController.TicketRestController
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param title
	 * @param desc
	 * @param requesterId
	 */
	public void handleCreateTicket(String title, String desc, String requesterId) {
		// TODO - implement TicketRestController.handleCreateTicket
		throw new UnsupportedOperationException();
	}

}