package users.application.ports.inbound;

public interface ManageAssigneeUseCase {

	/**
	 * 
	 * @param email
	 */
	void createAssigneeUser(String email);

	/**
	 * 
	 * @param agentId
	 * @param deptId
	 */
	void assignAgentToDept(String agentId, String deptId);

}