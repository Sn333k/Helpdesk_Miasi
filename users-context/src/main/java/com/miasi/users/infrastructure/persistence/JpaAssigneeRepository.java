package com.miasi.users.infrastructure.persistence;

import com.miasi.users.application.ports.outbound.IAssigneeRepository;
import com.miasi.users.domain.model.Assignee;
import com.miasi.users.domain.model.UserID;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class JpaAssigneeRepository implements IAssigneeRepository {

  private static final Logger LOG = Logger.getLogger(JpaAssigneeRepository.class.getName());

  private final EntityManagerFactory emf;

  public JpaAssigneeRepository(EntityManagerFactory emf) {
    this.emf = emf;
  }

  @Override
  public void save(Assignee assignee) {
    LOG.fine(() -> "Saving assignee: " + assignee.getId().id());
    EntityManager em = emf.createEntityManager();
    try {
      em.getTransaction().begin();
      em.merge(new AssigneeEntity(assignee));
      em.getTransaction().commit();
    } catch (Exception e) {
      em.getTransaction().rollback();
      throw e;
    } finally {
      em.close();
    }
  }

  @Override
  public Optional<Assignee> findById(UserID id) {
    LOG.fine(() -> "Finding assignee by id: " + id.id());
    EntityManager em = emf.createEntityManager();
    try {
      AssigneeEntity entity = em.find(AssigneeEntity.class, id.id());
      return Optional.ofNullable(entity).map(AssigneeEntity::toDomain);
    } finally {
      em.close();
    }
  }

  @Override
  public List<Assignee> findAvailableByArea(String area) {
    EntityManager em = emf.createEntityManager();
    try {
      return em
          .createQuery(
              "SELECT a FROM AssigneeEntity a"
                  + " WHERE a.agentStatus = :status"
                  + " AND a.accountStatus = :active"
                  + " AND EXISTS ("
                  + "   SELECT t FROM SupportTeamEntity t"
                  + "   WHERE LOWER(t.areaOfInterest) = LOWER(:area)"
                  + "   AND a.id MEMBER OF t.assignedAgentIds"
                  + ")",
              AssigneeEntity.class)
          .setParameter("status", "AVAILABLE")
          .setParameter("active", "ACTIVE")
          .setParameter("area", area)
          .getResultList()
          .stream()
          .map(AssigneeEntity::toDomain)
          .toList();
    } finally {
      em.close();
    }
  }
}
