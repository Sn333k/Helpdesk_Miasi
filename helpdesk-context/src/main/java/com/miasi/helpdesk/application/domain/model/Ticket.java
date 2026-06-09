package com.miasi.helpdesk.application.domain.model;

import com.miasi.helpdesk.application.domain.events.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Ticket {

  private final TicketID id;
  private TicketPriority priority;
  private TicketStatus status;
  private final RequesterID requesterId;
  private AssigneeID assigneeId;
  private final String title;
  private final String description;
  private final List<Comment> comments;
  private final Category category;
  private final SLA sla;
  private final List<DomainEvent> domainEvents;

  public Ticket(
      String title,
      String description,
      RequesterID requesterId,
      Category category,
      SLA sla,
      TicketPriority initialPriority) {
    if (title == null || title.isBlank())
      throw new IllegalArgumentException("Title must not be blank");
    if (requesterId == null) throw new IllegalArgumentException("RequesterID must not be null");

    this.id = new TicketID(UUID.randomUUID().toString());
    this.title = title;
    this.description = description;
    this.requesterId = requesterId;
    this.category = category;
    this.sla = sla;
    this.priority = initialPriority;
    this.status = TicketStatus.NEW;
    this.comments = new ArrayList<>();
    this.domainEvents = new ArrayList<>();
    this.assigneeId = null;

    domainEvents.add(new TicketCreated(id, requesterId, Instant.now()));
  }

  public void assignTo(AssigneeID agentId) {
    if (agentId == null) throw new IllegalArgumentException("AssigneeID must not be null");
    if (agentId.id().equals(requesterId.id()))
      throw new IllegalStateException("Assignee must differ from requester");
    if (!status.canTransitionTo(TicketStatus.ASSIGNED))
      throw new IllegalStateException("Cannot assign ticket in status " + status);

    TicketStatus old = this.status;
    this.assigneeId = agentId;
    this.status = TicketStatus.ASSIGNED;
    domainEvents.add(new TicketAssigned(id, agentId, Instant.now()));
    domainEvents.add(new TicketStatusChanged(id, old, TicketStatus.ASSIGNED, Instant.now()));
  }

  public void resolve(String resolution) {
    if (!status.canTransitionTo(TicketStatus.RESOLVED))
      throw new IllegalStateException("Cannot resolve ticket in status " + status);

    TicketStatus old = this.status;
    this.status = TicketStatus.RESOLVED;
    if (resolution != null && !resolution.isBlank())
      comments.add(new Comment("system", resolution, Instant.now()));
    domainEvents.add(new TicketStatusChanged(id, old, TicketStatus.RESOLVED, Instant.now()));
  }

  public void addComment(String authorId, String content) {
    if (status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED)
      throw new IllegalStateException("Cannot add comment to ticket in status " + status);
    comments.add(new Comment(authorId, content, Instant.now()));
    domainEvents.add(new TicketStatusChanged(id, status, status, Instant.now()));
  }

  public void escalatePriority() {
    if (status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED)
      throw new IllegalStateException("Cannot escalate priority of ticket in status " + status);

    this.priority = priority.escalate();
    if (sla.isBreached(Instant.now())) domainEvents.add(new SLABreached(id, Instant.now()));
  }

  public List<DomainEvent> popEvents() {
    List<DomainEvent> events = new ArrayList<>(domainEvents);
    domainEvents.clear();
    return Collections.unmodifiableList(events);
  }

  /** Reconstitutes a Ticket from persisted state without raising domain events. */
  public static Ticket reconstitute(
      TicketID id,
      String title,
      String description,
      RequesterID requesterId,
      AssigneeID assigneeId,
      Category category,
      SLA sla,
      TicketPriority priority,
      TicketStatus status,
      List<Comment> comments) {
    return new Ticket(
        id, title, description, requesterId, assigneeId, category, sla, priority, status, comments);
  }

  private Ticket(
      TicketID id,
      String title,
      String description,
      RequesterID requesterId,
      AssigneeID assigneeId,
      Category category,
      SLA sla,
      TicketPriority priority,
      TicketStatus status,
      List<Comment> comments) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.requesterId = requesterId;
    this.assigneeId = assigneeId;
    this.category = category;
    this.sla = sla;
    this.priority = priority;
    this.status = status;
    this.comments = new ArrayList<>(comments);
    this.domainEvents = new ArrayList<>();
  }

  public TicketID getId() {
    return id;
  }

  public TicketPriority getPriority() {
    return priority;
  }

  public TicketStatus getStatus() {
    return status;
  }

  public RequesterID getRequesterId() {
    return requesterId;
  }

  public AssigneeID getAssigneeId() {
    return assigneeId;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public List<Comment> getComments() {
    return Collections.unmodifiableList(comments);
  }

  public Category getCategory() {
    return category;
  }

  public SLA getSla() {
    return sla;
  }
}
