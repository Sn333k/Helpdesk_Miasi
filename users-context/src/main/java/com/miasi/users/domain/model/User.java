package com.miasi.users.domain.model;

import com.miasi.users.domain.events.AgentAvailabilityChanged;
import com.miasi.users.domain.events.DomainEvent;
import com.miasi.users.domain.events.UserDeactivated;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * User aggregate root. A user may simultaneously hold a requester role and an assignee (agent)
 * role. The domain logic enforces that a deactivated user cannot change availability.
 */
public class User {

  private final UserID id;
  private EmailAddress email;
  private AccountStatus accountStatus;
  private AgentStatus agentStatus;
  private final List<DomainEvent> domainEvents;

  public User(EmailAddress email) {
    if (email == null) throw new IllegalArgumentException("Email must not be null");

    this.id = new UserID(UUID.randomUUID().toString());
    this.email = email;
    this.accountStatus = AccountStatus.ACTIVE;
    this.agentStatus = AgentStatus.UNAVAILABLE;
    this.domainEvents = new ArrayList<>();
  }

  /** Package-private reconstitution constructor. */
  User(UserID id, EmailAddress email, AccountStatus accountStatus, AgentStatus agentStatus) {
    this.id = id;
    this.email = email;
    this.accountStatus = accountStatus;
    this.agentStatus = agentStatus;
    this.domainEvents = new ArrayList<>();
  }

  public static User reconstitute(
      UserID id, EmailAddress email, AccountStatus accountStatus, AgentStatus agentStatus) {
    return new User(id, email, accountStatus, agentStatus);
  }

  /**
   * Changes the user's email address. Invariant: account must be active; format validated by VO.
   */
  public void changeEmail(EmailAddress newEmail) {
    if (newEmail == null) throw new IllegalArgumentException("New email must not be null");
    if (!accountStatus.isActive())
      throw new IllegalStateException("Cannot change email of an inactive/suspended account");
    this.email = newEmail;
  }

  /**
   * Deactivates the user account. Invariant: only ACTIVE accounts can be deactivated. Sets agent
   * status to UNAVAILABLE.
   */
  public void deactivate() {
    if (accountStatus != AccountStatus.ACTIVE)
      throw new IllegalStateException("Only ACTIVE accounts can be deactivated");

    this.accountStatus = AccountStatus.INACTIVE;
    this.agentStatus = AgentStatus.UNAVAILABLE;
    domainEvents.add(new UserDeactivated(id, AccountStatus.INACTIVE, Instant.now()));
  }

  /**
   * Changes the agent's availability status. Invariant: only ACTIVE accounts may change
   * availability; cannot set the same status twice.
   */
  public void changeAgentStatus(AgentStatus newStatus) {
    if (newStatus == null) throw new IllegalArgumentException("Agent status must not be null");
    if (!accountStatus.isActive())
      throw new IllegalStateException("Inactive accounts cannot change agent status");
    if (this.agentStatus == newStatus)
      throw new IllegalStateException("Agent status is already " + newStatus);

    this.agentStatus = newStatus;
    domainEvents.add(new AgentAvailabilityChanged(id, newStatus, Instant.now()));
  }

  public List<DomainEvent> popEvents() {
    List<DomainEvent> events = new ArrayList<>(domainEvents);
    domainEvents.clear();
    return Collections.unmodifiableList(events);
  }

  public UserID getId() {
    return id;
  }

  public EmailAddress getEmail() {
    return email;
  }

  public AccountStatus getAccountStatus() {
    return accountStatus;
  }

  public AgentStatus getAgentStatus() {
    return agentStatus;
  }
}
