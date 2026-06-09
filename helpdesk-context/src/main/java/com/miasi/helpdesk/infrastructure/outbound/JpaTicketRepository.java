package com.miasi.helpdesk.infrastructure.outbound;

import com.miasi.helpdesk.application.domain.model.*;
import com.miasi.helpdesk.application.ports.outbound.ITicketRepository;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class JpaTicketRepository implements ITicketRepository {

  private final EntityManagerFactory emf;

  public JpaTicketRepository(EntityManagerFactory emf) {
    this.emf = emf;
  }

  @Override
  public void save(Ticket ticket) {
    EntityManager em = emf.createEntityManager();
    try {
      em.getTransaction().begin();
      em.merge(new TicketEntity(ticket));
      em.getTransaction().commit();
    } catch (Exception e) {
      em.getTransaction().rollback();
      throw e;
    } finally {
      em.close();
    }
  }

  @Override
  public Optional<Ticket> findById(TicketID id) {
    EntityManager em = emf.createEntityManager();
    try {
      TicketEntity entity = em.find(TicketEntity.class, id.id());
      return Optional.ofNullable(entity).map(TicketEntity::toDomain);
    } finally {
      em.close();
    }
  }

  @Override
  public List<Ticket> findAllBreachingSla(Instant now) {
    EntityManager em = emf.createEntityManager();
    try {
      return em
          .createQuery(
              "SELECT t FROM TicketEntity t WHERE t.slaDeadline < :now "
                  + "AND t.status NOT IN (:resolved, :closed)",
              TicketEntity.class)
          .setParameter("now", now)
          .setParameter("resolved", TicketStatus.RESOLVED)
          .setParameter("closed", TicketStatus.CLOSED)
          .getResultList()
          .stream()
          .map(TicketEntity::toDomain)
          .toList();
    } finally {
      em.close();
    }
  }
}
