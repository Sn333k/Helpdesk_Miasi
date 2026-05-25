package users.infrastructure.adapters.inbound;

import users.application.ports.outbound.IDepartmentRepository;
import users.application.ports.outbound.IUserRepository;
import users.domain.services.*;

public class HelpdeskInternalClientAdapter {

	private IDepartmentRepository departmentRepository;
	private IUserRepository userRepository;
	private StaffAssignmentService staffAssignmentService;

	/**
	 * 
	 * @param category
	 */
	public List<AgentSnapshotDto> getAvailableAgentsForCategory(String category) {
		// TODO - implement HelpdeskInternalClientAdapter.getAvailableAgentsForCategory
		throw new UnsupportedOperationException();
	}

}