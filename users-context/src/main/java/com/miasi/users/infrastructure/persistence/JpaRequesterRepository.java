package com.miasi.users.infrastructure.persistence;

import com.miasi.users.application.ports.outbound.IRequesterRepository;
import com.miasi.users.domain.model.Requester;
import com.miasi.users.domain.model.UserID;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.Optional;
import java.util.logging.Logger;

public class JpaRequesterRepository implements IRequesterRepository {

  private static final Logger LOG = Logger.getLogger(JpaRequesterRepository.class.getName());

  private final EntityManagerFactory emf;

  public JpaRequesterRepository(EntityManagerFactory emf) {
    this.emf = emf;
  }

  @Override
  public void save(Requester requester) {
    LOG.fine(() -> "Saving requester: " + requester.getId().id());
    EntityManager em = emf.createEntityManager();
    try {
      em.getTransaction().begin();
      em.merge(new RequesterEntity(requester));
      em.getTransaction().commit();
    } catch (Exception e) {
      em.getTransaction().rollback();
      throw e;
    } finally {
      em.close();
    }
  }

  @Override
  public Optional<Requester> findById(UserID id) {
    LOG.fine(() -> "Finding requester by id: " + id.id());
    EntityManager em = emf.createEntityManager();
    try {
      RequesterEntity entity = em.find(RequesterEntity.class, id.id());
      return Optional.ofNullable(entity).map(RequesterEntity::toDomain);
    } finally {
      em.close();
    }
  }
}
