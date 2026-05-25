package users.application.services;

import users.application.ports.inbound.ChangeAgentAvailabilityUseCase;
import users.application.ports.inbound.ManageAssigneeUseCase;
import users.application.ports.outbound.IDepartmentRepository;
import users.application.ports.outbound.IUserRepository;
import users.usersApplication.ports.inbound.*;
import users.usersApplication.ports.outbound.*;

public class UserApplicationService implements ManageAssigneeUseCase, ChangeAgentAvailabilityUseCase {

	private IUserRepository userRepository;
	private IDepartmentRepository departmentRepository;

	/**
	 * 
	 * @param agentId
	 * @param status
	 */
	public void changeStatus(String agentId, String status) {
		// TODO - implement UserApplicationService.changeStatus
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param agentId
	 * @param deptId
	 */
	public void assignAgentToDept(String agentId, String deptId) {
		// TODO - implement UserApplicationService.assignAgentToDept
		throw new UnsupportedOperationException();
	}

}