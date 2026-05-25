package users.domain.model;

public class AssigneeRole {

	private UserID assigneeId;
	private int currentLoad;
	private AgentStatus status;
	private Set<SupportTeam> assignedSupportTeam;

	/**
	 * 
	 * @param requiredSupportTeam
	 */
	public Boolean hasDepartment(SupportTeam requiredSupportTeam) {

	}

	/**
	 * 
	 * @param newTicket
	 */
	public Boolean canTakeMoreWork(Ticket newTicket) {
		// TODO - implement Assignee.canTakeMoreWork
		throw new UnsupportedOperationException();
	}

	public Boolean isAvailable() {
		// TODO - implement Assignee.isAvailable
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param supportTeamName
	 */
	public void deleteSupportTeam(String supportTeamName) {

	}

	/**
	 * 
	 * @param supportTeamName
	 */
	public void addSupportTeam(String supportTeamName) {

	}

}