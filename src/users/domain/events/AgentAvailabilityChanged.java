package users.domain.events;

import users.domain.model.*;

public class AgentAvailabilityChanged {

	private UserID userId;
	private AgentStatus newStatus;
	private Instant occurredAt;

}