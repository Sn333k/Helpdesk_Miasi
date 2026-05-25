package users.domain.model;

public class SupportTeam {

	private SupportTeamID supportTeamID;
	private String name;
	private String areaOfInterest;
	private Set<UserID> assignedAgents;

	/**
	 * 
	 * @param agentId
	 */
	public void assignAgent(UserID agentId) {
		// TODO - implement Department.assignAgent
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param agentId
	 */
	public void removeAgent(UserID agentId) {
		// TODO - implement Department.removeAgent
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param areaOfInterest
	 */
	public void supportsAreaOfInterest(String areaOfInterest) {
		// TODO - implement Department.supportsAreaOfInterest
		throw new UnsupportedOperationException();
	}

}