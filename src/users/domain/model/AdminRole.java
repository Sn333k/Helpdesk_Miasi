package users.domain.model;

public class AdminRole {

	private UserID adminId;

	/**
	 * 
	 * @param userID
	 */
	public void deactivateUser(UserID userID) {
		// TODO - implement Admin.deactivateUser
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param assigneeID
	 * @param supportTeamID
	 */
	public void assignAssigneeToDepartment(UserID assigneeID, SupportTeamID supportTeamID) {
		// TODO - implement Admin.assignAssigneeToDepartment
		throw new UnsupportedOperationException();
	}

	public void purgeDeletedTickets() {
		// TODO - implement Admin.purgeDeletedTickets
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param ticketID
	 * @param newAssigneeID
	 */
	public void forceReassign(TicketID ticketID, UserID newAssigneeID) {
		// TODO - implement Admin.forceReassign
		throw new UnsupportedOperationException();
	}

}