package users.application.ports.outbound;

import users.domain.model.*;

public interface IDepartmentRepository {

	/**
	 * 
	 * @param id
	 */
	Optional<SupportTeam> findById(DepartmentId id);

	List<SupportTeam> findAll();

	/**
	 * 
	 * @param department
	 */
	void save(SupportTeam department);

}