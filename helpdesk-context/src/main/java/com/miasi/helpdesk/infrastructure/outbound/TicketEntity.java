package com.miasi.helpdesk.infrastructure.outbound;

import com.miasi.helpdesk.domain.model.*;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tickets")
public class TicketEntity {

  @Id private String id;

  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  private String requesterId;
  private String assigneeId;

  @Enumerated(EnumType.STRING)
  private TicketStatus status;

  @Enumerated(EnumType.STRING)
  private TicketPriority priority;

  private String categoryName;
  private Instant slaDeadline;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "ticket_comments", joinColumns = @JoinColumn(name = "ticket_id"))
  private List<CommentEmbeddable> comments = new ArrayList<>();

  public TicketEntity() {}

  TicketEntity(Ticket ticket) {
    this.id = ticket.getId().id();
    this.title = ticket.getTitle();
    this.description = ticket.getDescription();
    this.requesterId = ticket.getRequesterId().id();
    this.assigneeId = ticket.getAssigneeId() != null ? ticket.getAssigneeId().id() : null;
    this.status = ticket.getStatus();
    this.priority = ticket.getPriority();
    this.categoryName = ticket.getCategory().name();
    this.slaDeadline = ticket.getSla().deadline();
    ticket.getComments().forEach(c -> comments.add(new CommentEmbeddable(c)));
  }

  Ticket toDomain() {
    AssigneeID aid = assigneeId != null ? new AssigneeID(assigneeId) : null;
    List<Comment> domainComments = comments.stream().map(CommentEmbeddable::toDomain).toList();
    return Ticket.reconstitute(
        new TicketID(id),
        title,
        description,
        new RequesterID(requesterId),
        aid,
        new Category(categoryName),
        new SLA(slaDeadline),
        priority,
        status,
        domainComments);
  }
}
