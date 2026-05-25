package helpdesk.domain.model;

import users.domain.model.*;

public class Ticket {

	private TicketID iD;
	private TicketPriority priority;
	private TicketStatus status;
	private UserID requester;
	private UserID assignee;
	private String title;
	private String description;
	private List<Comment> comments;
	private Set<Category> categories;
	private SLA sla;

	/**
	 * 
	 * @param agentID
	 */
	public void assignTo(UserID agentID) {
		// TODO - implement Ticket.assignTo
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param resolution
	 */
	public void resolve(String resolution) {
		// TODO - implement Ticket.resolve
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param authorID
	 * @param content
	 */
	public void addComment(UserID authorID, String content) {
		// TODO - implement Ticket.addComment
		throw new UnsupportedOperationException();
	}

	public void escalatePriority() {
		// TODO - implement Ticket.escalatePriority
		throw new UnsupportedOperationException();
	}

}