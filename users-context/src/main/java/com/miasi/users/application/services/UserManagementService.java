package com.miasi.users.application.services;

import com.miasi.users.application.ports.inbound.AssignAgentToTeamUseCase;
import com.miasi.users.application.ports.inbound.ChangeAgentAvailabilityUseCase;
import com.miasi.users.application.ports.inbound.ChangeEmailUseCase;
import com.miasi.users.application.ports.inbound.CreateAssigneeUseCase;
import com.miasi.users.application.ports.inbound.CreateRequesterUseCase;
import com.miasi.users.application.ports.inbound.CreateSupportTeamUseCase;
import com.miasi.users.application.ports.inbound.GetAvailableAgentsUseCase;
import com.miasi.users.application.ports.inbound.RemoveAgentFromTeamUseCase;
import com.miasi.users.application.ports.outbound.IAssigneeRepository;
import com.miasi.users.application.ports.outbound.IEventPublisher;
import com.miasi.users.application.ports.outbound.IRequesterRepository;
import com.miasi.users.application.ports.outbound.ISupportTeamRepository;
import com.miasi.users.domain.model.AgentSnapshot;
import com.miasi.users.domain.model.AgentStatus;
import com.miasi.users.domain.model.Assignee;
import com.miasi.users.domain.model.AssigneeFactory;
import com.miasi.users.domain.model.EmailAddress;
import com.miasi.users.domain.model.Requester;
import com.miasi.users.domain.model.RequesterFactory;
import com.miasi.users.domain.model.SupportTeam;
import com.miasi.users.domain.model.SupportTeamID;
import com.miasi.users.domain.model.UserID;
import com.miasi.users.domain.model.UserNotFoundException;
import java.util.List;
import java.util.logging.Logger;

public class UserManagementService
    implements CreateRequesterUseCase,
        CreateAssigneeUseCase,
        ChangeEmailUseCase,
        ChangeAgentAvailabilityUseCase,
        AssignAgentToTeamUseCase,
        RemoveAgentFromTeamUseCase,
        CreateSupportTeamUseCase,
        GetAvailableAgentsUseCase {

  private static final Logger LOG = Logger.getLogger(UserManagementService.class.getName());

  private final IRequesterRepository requesterRepository;
  private final IAssigneeRepository assigneeRepository;
  private final ISupportTeamRepository teamRepository;
  private final IEventPublisher eventPublisher;

  public UserManagementService(
      IRequesterRepository requesterRepository,
      IAssigneeRepository assigneeRepository,
      ISupportTeamRepository teamRepository,
      IEventPublisher eventPublisher) {
    this.requesterRepository = requesterRepository;
    this.assigneeRepository = assigneeRepository;
    this.teamRepository = teamRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public UserID createRequester(String email) {
    LOG.info(() -> "Creating requester with email: " + email);
    Requester requester = RequesterFactory.create(new EmailAddress(email));
    requesterRepository.save(requester);
    requester.popEvents().forEach(eventPublisher::publish);
    return requester.getId();
  }

  @Override
  public UserID createAssignee(String agentId, AgentStatus initialStatus) {
    Assignee assignee =
        agentId == null
            ? AssigneeFactory.create(initialStatus)
            : AssigneeFactory.create(new UserID(agentId), initialStatus);
    assigneeRepository.save(assignee);
    assignee.popEvents().forEach(eventPublisher::publish);
    LOG.info(() -> "Created assignee: " + assignee.getId().id());
    return assignee.getId();
  }

  @Override
  public void changeEmail(UserID id, EmailAddress newEmail) {
    LOG.info(() -> "Changing email for requester: " + id.id());
    Requester requester =
        requesterRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    requester.changeEmail(newEmail);
    requesterRepository.save(requester);
    requester.popEvents().forEach(eventPublisher::publish);
  }

  @Override
  public void changeAgentStatus(UserID agentId, AgentStatus newStatus) {
    LOG.info(() -> "Changing agent status for: " + agentId.id() + " to " + newStatus);
    Assignee assignee =
        assigneeRepository.findById(agentId).orElseThrow(() -> new UserNotFoundException(agentId));
    assignee.changeAgentStatus(newStatus);
    assigneeRepository.save(assignee);
    assignee.popEvents().forEach(eventPublisher::publish);
  }

  @Override
  public void assignAgentToTeam(UserID agentId, SupportTeamID teamId) {
    LOG.info(() -> "Assigning agent " + agentId.id() + " to team " + teamId.id());
    assigneeRepository.findById(agentId).orElseThrow(() -> new UserNotFoundException(agentId));
    SupportTeam team =
        teamRepository
            .findById(teamId)
            .orElseThrow(
                () -> new IllegalArgumentException("SupportTeam not found: " + teamId.id()));
    team.assignAgent(agentId);
    teamRepository.save(team);
    team.popEvents().forEach(eventPublisher::publish);
  }

  @Override
  public void removeAgentFromTeam(UserID agentId, SupportTeamID teamId) {
    LOG.info(() -> "Removing agent " + agentId.id() + " from team " + teamId.id());
    SupportTeam team =
        teamRepository
            .findById(teamId)
            .orElseThrow(
                () -> new IllegalArgumentException("SupportTeam not found: " + teamId.id()));
    team.removeAgent(agentId);
    teamRepository.save(team);
  }

  @Override
  public SupportTeamID createTeam(String name, String areaOfInterest) {
    LOG.info(() -> "Creating support team: " + name + " area=" + areaOfInterest);
    SupportTeam team = new SupportTeam(name, areaOfInterest);
    teamRepository.save(team);
    team.popEvents().forEach(eventPublisher::publish);
    return team.getId();
  }

  @Override
  public List<AgentSnapshot> getAvailableAgentsForArea(String areaOfInterest) {
    return assigneeRepository.findAvailableByArea(areaOfInterest).stream()
        .map(a -> new AgentSnapshot(a.getId().id(), "", true))
        .toList();
  }
}
