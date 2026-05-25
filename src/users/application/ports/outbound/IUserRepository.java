package users.application.ports.outbound;

import users.domain.model.*;

public interface IUserRepository {

	/**
	 * 
	 * @param id
	 */
	Optional<User> findById(UserID id);

	/**
	 * 
	 * @param ids
	 */
	void findAllAgentsByAddresses(List<UserID> ids);

	/**
	 * 
	 * @param user
	 */
	void save(User user);

}