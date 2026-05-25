package helpdesk.infrastructure.outbound;

import helpdesk.application.ports.outbound.IAgentAvailabilityProvider;

public class AgentAvailabilityAdapter implements IAgentAvailabilityProvider {

	private HttpClient restClient;

	/**
	 * 
	 * @param category
	 */
	public List<AgentSnapshot> getAvailableAgentsForCategory(Category category) {
		// TODO - implement AgentAvailabilityAdapter.getAvailableAgentsForCategory
		throw new UnsupportedOperationException();
	}

}