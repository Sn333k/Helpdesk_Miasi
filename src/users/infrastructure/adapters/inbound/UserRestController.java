package users.infrastructure.adapters.inbound;

import users.application.ports.inbound.ChangeAgentAvailabilityUseCase;

public class UserRestController {

	private ManageStaffUseCase manageStaffUseCase;
	private ChangeAgentAvailabilityUseCase changeAgentAvailabilityUseCase;

	/**
	 * 
	 * @param request
	 */
	public ResponseEntity postNewUser(CreateUserDto request) {
		// TODO - implement UserRestController.postNewUser
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param agentId
	 * @param statusDto
	 */
	public ResponseEntity putAgentStatus(String agentId, StatusDto statusDto) {
		// TODO - implement UserRestController.putAgentStatus
		throw new UnsupportedOperationException();
	}

}