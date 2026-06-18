package com.miasi.users.domain.model;

import com.miasi.users.domain.events.DomainEvent;
import com.miasi.users.domain.events.RequesterDeactivated;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Requester {

  private final UserID id;
  private EmailAddress email;
  private AccountStatus accountStatus;
  private final List<DomainEvent> domainEvents;

  Requester(EmailAddress email) {
    if (email == null) throw new IllegalArgumentException("Email must not be null");
    this.id = new UserID(UUID.randomUUID().toString());
    this.email = email;
    this.accountStatus = AccountStatus.ACTIVE;
    this.domainEvents = new ArrayList<>();
  }

  private Requester(UserID id, EmailAddress email, AccountStatus accountStatus) {
    this.id = id;
    this.email = email;
    this.accountStatus = accountStatus;
    this.domainEvents = new ArrayList<>();
  }

  public static Requester reconstitute(UserID id, EmailAddress email, AccountStatus accountStatus) {
    return new Requester(id, email, accountStatus);
  }

  public void changeEmail(EmailAddress newEmail) {
    if (newEmail == null) throw new IllegalArgumentException("New email must not be null");
    if (!accountStatus.isActive())
      throw new IllegalStateException("Cannot change email of an inactive or suspended account");
    this.email = newEmail;
  }

  public void deactivate() {
    if (accountStatus != AccountStatus.ACTIVE)
      throw new IllegalStateException("Only ACTIVE accounts can be deactivated");
    this.accountStatus = AccountStatus.INACTIVE;
    domainEvents.add(new RequesterDeactivated(id, Instant.now()));
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
}
