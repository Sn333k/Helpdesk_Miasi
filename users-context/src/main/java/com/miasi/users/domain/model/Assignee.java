package com.miasi.users.domain.model;

import com.miasi.users.domain.events.AgentAvailabilityChanged;
import com.miasi.users.domain.events.AssigneeDeactivated;
import com.miasi.users.domain.events.DomainEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Assignee {

  private final UserID id;
  private AgentStatus agentStatus;
  private AccountStatus accountStatus;
  private final List<DomainEvent> domainEvents;

  Assignee(AgentStatus initialStatus) {
    if (initialStatus == null)
      throw new IllegalArgumentException("Initial status must not be null");
    this.id = new UserID(UUID.randomUUID().toString());
    this.agentStatus = initialStatus;
    this.accountStatus = AccountStatus.ACTIVE;
    this.domainEvents = new ArrayList<>();
  }

  Assignee(UserID id, AgentStatus initialStatus) {
    if (id == null) throw new IllegalArgumentException("UserID must not be null");
    if (initialStatus == null)
      throw new IllegalArgumentException("Initial status must not be null");
    this.id = id;
    this.agentStatus = initialStatus;
    this.accountStatus = AccountStatus.ACTIVE;
    this.domainEvents = new ArrayList<>();
  }

  private Assignee(UserID id, AgentStatus agentStatus, AccountStatus accountStatus) {
    this.id = id;
    this.agentStatus = agentStatus;
    this.accountStatus = accountStatus;
    this.domainEvents = new ArrayList<>();
  }

  public static Assignee reconstitute(
      UserID id, AgentStatus agentStatus, AccountStatus accountStatus) {
    return new Assignee(id, agentStatus, accountStatus);
  }

  public void changeAgentStatus(AgentStatus newStatus) {
    if (newStatus == null) throw new IllegalArgumentException("Agent status must not be null");
    if (!accountStatus.isActive())
      throw new IllegalStateException("Inactive or suspended accounts cannot change agent status");
    if (this.agentStatus == newStatus)
      throw new IllegalStateException("Agent status is already " + newStatus);
    this.agentStatus = newStatus;
    domainEvents.add(new AgentAvailabilityChanged(id, newStatus, Instant.now()));
  }

  public void deactivate() {
    if (accountStatus != AccountStatus.ACTIVE)
      throw new IllegalStateException("Only ACTIVE accounts can be deactivated");
    this.accountStatus = AccountStatus.INACTIVE;
    this.agentStatus = AgentStatus.UNAVAILABLE;
    domainEvents.add(new AssigneeDeactivated(id, Instant.now()));
    domainEvents.add(new AgentAvailabilityChanged(id, AgentStatus.UNAVAILABLE, Instant.now()));
  }

  public void suspend() {
    if (accountStatus == AccountStatus.SUSPENDED)
      throw new IllegalStateException("Account is already SUSPENDED");
    this.accountStatus = AccountStatus.SUSPENDED;
    this.agentStatus = AgentStatus.UNAVAILABLE;
    domainEvents.add(new AgentAvailabilityChanged(id, AgentStatus.UNAVAILABLE, Instant.now()));
  }

  public List<DomainEvent> popEvents() {
    List<DomainEvent> events = new ArrayList<>(domainEvents);
    domainEvents.clear();
    return Collections.unmodifiableList(events);
  }

  public UserID getId() {
    return id;
  }

  public AgentStatus getAgentStatus() {
    return agentStatus;
  }

  public AccountStatus getAccountStatus() {
    return accountStatus;
  }
}
