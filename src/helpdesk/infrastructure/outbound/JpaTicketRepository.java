package helpdesk.infrastructure.outbound;

import helpdesk.application.ports.outbound.ITicketRepository;
import helpdesk.application.domain.model.Ticket;
import helpdesk.application.domain.model.TicketID;

public class JpaTicketRepository implements ITicketRepository {

	/**
	 * 
	 * @param ticket
	 */
	public void save(Ticket ticket) {
		// TODO - implement JpaTicketRepository.save
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param id
	 */
	public Optional<Ticket> findById(TicketID id) {
		// TODO - implement JpaTicketRepository.findById
		throw new UnsupportedOperationException();
	}

}