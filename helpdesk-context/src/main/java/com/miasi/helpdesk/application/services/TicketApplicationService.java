package com.miasi.helpdesk.application.services;

import com.miasi.helpdesk.application.domain.model.*;
import com.miasi.helpdesk.application.domain.services.StaffAssignmentService;
import com.miasi.helpdesk.application.ports.inbound.AddCommentUseCase;
import com.miasi.helpdesk.application.ports.inbound.AssignTicketUseCase;
import com.miasi.helpdesk.application.ports.inbound.CreateTicketUseCase;
import com.miasi.helpdesk.application.ports.inbound.EscalateTicketUseCase;
import com.miasi.helpdesk.application.ports.inbound.GetTicketUseCase;
import com.miasi.helpdesk.application.ports.inbound.ResolveTicketUseCase;
import com.miasi.helpdesk.application.ports.inbound.TicketView;
import com.miasi.helpdesk.application.ports.outbound.IAgentAvailabilityProvider;
import com.miasi.helpdesk.application.ports.outbound.INotificationSender;
import com.miasi.helpdesk.application.ports.outbound.ITicketRepository;
import java.util.List;
import java.util.logging.Logger;

public class TicketApplicationService
    implements CreateTicketUseCase,
        AssignTicketUseCase,
        EscalateTicketUseCase,
        ResolveTicketUseCase,
        AddCommentUseCase,
        GetTicketUseCase {

  private static final Logger LOG = Logger.getLogger(TicketApplicationService.class.getName());

  private final TicketFactory ticketFactory;
  private final ITicketRepository ticketRepository;
  private final IAgentAvailabilityProvider agentProvider;
  private final INotificationSender notificationSender;
  private final StaffAssignmentService staffAssignmentService;
  private final Category defaultCategory;

  public TicketApplicationService(
      TicketFactory ticketFactory,
      ITicketRepository ticketRepository,
      IAgentAvailabilityProvider agentProvider,
      INotificationSender notificationSender,
      StaffAssignmentService staffAssignmentService,
      Category defaultCategory) {
    this.ticketFactory = ticketFactory;
    this.ticketRepository = ticketRepository;
    this.agentProvider = agentProvider;
    this.notificationSender = notificationSender;
    this.staffAssignmentService = staffAssignmentService;
    this.defaultCategory = defaultCategory;
  }

  @Override
  public TicketID execute(String title, String description, String requesterId) {
    LOG.info(() -> "Creating ticket: requesterId=" + requesterId);
    Ticket ticket =
        ticketFactory.create(title, description, new RequesterID(requesterId), defaultCategory);
    ticketRepository.save(ticket);
    ticket.popEvents().forEach(notificationSender::sendNotification);
    LOG.info(() -> "Ticket created: " + ticket.getId().id());
    return ticket.getId();
  }

  @Override
  public void execute(TicketID ticketId) {
    LOG.info(() -> "Assigning ticket: " + ticketId.id());
    Ticket ticket =
        ticketRepository
            .findById(ticketId)
            .orElseThrow(() -> new TicketNotFoundException(ticketId));
    List<AgentSnapshot> candidates =
        agentProvider.getAvailableAgentsForCategory(ticket.getCategory());
    AgentSnapshot agent = staffAssignmentService.findOptimalAgent(ticket.getCategory(), candidates);
    ticket.assignTo(new AssigneeID(agent.agentId()));
    ticketRepository.save(ticket);
    ticket.popEvents().forEach(notificationSender::sendNotification);
    LOG.info(() -> "Ticket " + ticketId.id() + " assigned to " + agent.agentId());
  }

  @Override
  public void escalate(TicketID ticketId) {
    LOG.warning(() -> "SLA-breached ticket escalated: " + ticketId.id());
    Ticket ticket =
        ticketRepository
            .findById(ticketId)
            .orElseThrow(() -> new TicketNotFoundException(ticketId));
    ticket.escalatePriority();
    ticketRepository.save(ticket);
    ticket.popEvents().forEach(notificationSender::sendNotification);
  }

  @Override
  public void execute(TicketID ticketId, String resolution) {
    LOG.info(() -> "Resolving ticket: " + ticketId.id());
    Ticket ticket =
        ticketRepository
            .findById(ticketId)
            .orElseThrow(() -> new TicketNotFoundException(ticketId));
    ticket.resolve(resolution);
    ticketRepository.save(ticket);
    ticket.popEvents().forEach(notificationSender::sendNotification);
  }

  @Override
  public void execute(TicketID ticketId, String authorId, String content) {
    Ticket ticket =
        ticketRepository
            .findById(ticketId)
            .orElseThrow(() -> new TicketNotFoundException(ticketId));
    ticket.addComment(authorId, content);
    ticketRepository.save(ticket);
    ticket.popEvents().forEach(notificationSender::sendNotification);
    LOG.info(() -> "Comment added to ticket: " + ticketId.id());
  }

  @Override
  public TicketView get(TicketID ticketId) {
    Ticket ticket =
        ticketRepository
            .findById(ticketId)
            .orElseThrow(() -> new TicketNotFoundException(ticketId));
    List<TicketView.CommentView> comments =
        ticket.getComments().stream()
            .map(c -> new TicketView.CommentView(c.authorId(), c.content(), c.createdAt()))
            .toList();
    return new TicketView(
        ticket.getId().id(),
        ticket.getTitle(),
        ticket.getDescription(),
        ticket.getStatus().name(),
        ticket.getPriority().name(),
        ticket.getRequesterId().id(),
        ticket.getAssigneeId() != null ? ticket.getAssigneeId().id() : null,
        ticket.getCategory().name(),
        ticket.getSla().deadline(),
        comments);
  }
}
