package helpdesk.application.ports.outbound;

public interface IAgentAvailabilityProvider {

	/**
	 * 
	 * @param cat
	 */
	List<AgentSnapshot> getAvailableAgentsForCategory(Category cat);

}