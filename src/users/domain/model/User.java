package users.domain.model;

public class User {

	private UserID userId;
	private EmailAddress email;
	private AccountStatus accountStatus;
	private String credentials;
	private RequesterRole requesterRole;
	private AssigneeRole assigneeRole;
	private AdminRole adminRole;

	/**
	 * 
	 * @param newEmail
	 */
	public EmailAddress changeEmail(String newEmail) {
		// TODO - implement User.changeEmail
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param reason
	 */
	public void deactivate(String reason) {
		// TODO - implement User.deactivate
		throw new UnsupportedOperationException();
	}

	public void assignGlobalRole() {
		// TODO - implement User.assignGlobalRole
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param status
	 */
	public void changeStatus(String status) {
		// TODO - implement User.changeStatus
		throw new UnsupportedOperationException();
	}

}