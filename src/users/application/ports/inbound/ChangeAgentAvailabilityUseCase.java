package users.application.ports.inbound;

public interface ChangeAgentAvailabilityUseCase {

	/**
	 * 
	 * @param agentId
	 * @param status
	 */
	void changeStatus(String agentId, String status);

}