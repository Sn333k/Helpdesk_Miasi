package com.miasi.users.infrastructure.persistence;

import com.miasi.users.application.ports.outbound.ISupportTeamRepository;
import com.miasi.users.domain.model.SupportTeam;
import com.miasi.users.domain.model.SupportTeamID;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class JpaSupportTeamRepository implements ISupportTeamRepository {

  private static final Logger LOG = Logger.getLogger(JpaSupportTeamRepository.class.getName());

  private final EntityManagerFactory emf;

  public JpaSupportTeamRepository(EntityManagerFactory emf) {
    this.emf = emf;
  }

  @Override
  public void save(SupportTeam team) {
    LOG.fine(() -> "Saving support team: " + team.getId().id());
    EntityManager em = emf.createEntityManager();
    try {
      em.getTransaction().begin();
      em.merge(new SupportTeamEntity(team));
      em.getTransaction().commit();
    } catch (Exception e) {
      em.getTransaction().rollback();
      throw e;
    } finally {
      em.close();
    }
  }

  @Override
  public Optional<SupportTeam> findById(SupportTeamID id) {
    LOG.fine(() -> "Finding support team by id: " + id.id());
    EntityManager em = emf.createEntityManager();
    try {
      SupportTeamEntity entity = em.find(SupportTeamEntity.class, id.id());
      return Optional.ofNullable(entity).map(SupportTeamEntity::toDomain);
    } finally {
      em.close();
    }
  }

  @Override
  public List<SupportTeam> findByAreaOfInterest(String area) {
    EntityManager em = emf.createEntityManager();
    try {
      return em
          .createQuery(
              "SELECT t FROM SupportTeamEntity t" + " WHERE LOWER(t.areaOfInterest) = LOWER(:area)",
              SupportTeamEntity.class)
          .setParameter("area", area)
          .getResultList()
          .stream()
          .map(SupportTeamEntity::toDomain)
          .toList();
    } finally {
      em.close();
    }
  }
}
