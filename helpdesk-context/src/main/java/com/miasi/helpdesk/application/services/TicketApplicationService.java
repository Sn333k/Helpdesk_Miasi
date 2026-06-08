package com.miasi.helpdesk.application.services;

import com.miasi.helpdesk.application.domain.model.*;
import com.miasi.helpdesk.application.domain.services.StaffAssignmentService;
import com.miasi.helpdesk.application.ports.inbound.AssignTicketUseCase;
import com.miasi.helpdesk.application.ports.inbound.CreateTicketUseCase;
import com.miasi.helpdesk.application.ports.inbound.EscalateTicketUseCase;
import com.miasi.helpdesk.application.ports.inbound.ResolveTicketUseCase;
import com.miasi.helpdesk.application.ports.outbound.IAgentAvailabilityProvider;
import com.miasi.helpdesk.application.ports.outbound.INotificationSender;
import com.miasi.helpdesk.application.ports.outbound.ITicketRepository;
import java.util.List;

public class TicketApplicationService
    implements CreateTicketUseCase,
        AssignTicketUseCase,
        EscalateTicketUseCase,
        ResolveTicketUseCase {

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
    Ticket ticket =
        ticketFactory.create(title, description, new RequesterID(requesterId), defaultCategory);
    ticketRepository.save(ticket);
    ticket.popEvents().forEach(notificationSender::sendNotification);
    return ticket.getId();
  }

  @Override
  public void execute(TicketID ticketId) {
    Ticket ticket =
        ticketRepository
            .findById(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId.id()));
    List<AgentSnapshot> candidates =
        agentProvider.getAvailableAgentsForCategory(ticket.getCategory());
    AgentSnapshot agent = staffAssignmentService.findOptimalAgent(ticket.getCategory(), candidates);
    ticket.assignTo(new AssigneeID(agent.agentId()));
    ticketRepository.save(ticket);
    ticket.popEvents().forEach(notificationSender::sendNotification);
  }

  @Override
  public void escalate(TicketID ticketId) {
    Ticket ticket =
        ticketRepository
            .findById(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId.id()));
    ticket.escalatePriority();
    ticketRepository.save(ticket);
    ticket.popEvents().forEach(notificationSender::sendNotification);
  }

  @Override
  public void execute(TicketID ticketId, String resolution) {
    Ticket ticket =
        ticketRepository
            .findById(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId.id()));
    ticket.resolve(resolution);
    ticketRepository.save(ticket);
    ticket.popEvents().forEach(notificationSender::sendNotification);
  }
}
