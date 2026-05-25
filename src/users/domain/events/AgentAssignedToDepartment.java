package users.domain.events;

import users.domain.model.*;

public class AgentAssignedToDepartment {

	private UserID userId;
	private DepartmentId departmentId;
	private Instant occurredAt;

}