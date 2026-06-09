package helpdesk.application.ports.outbound;

import helpdesk.application.domain.model.Ticket;
import helpdesk.application.domain.model.TicketID;

public interface ITicketRepository {

	/**
	 * 
	 * @param ticket
	 */
	void save(Ticket ticket);

	/**
	 * 
	 * @param id
	 */
	Optional<Ticket> findById(TicketID id);

}