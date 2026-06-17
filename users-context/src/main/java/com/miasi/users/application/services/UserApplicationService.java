package com.miasi.users.application.services;

import com.miasi.users.application.ports.inbound.AssignAgentToTeamUseCase;
import com.miasi.users.application.ports.inbound.ChangeAgentAvailabilityUseCase;
import com.miasi.users.application.ports.inbound.GetAvailableAgentsUseCase;
import com.miasi.users.application.ports.outbound.IEventPublisher;
import com.miasi.users.application.ports.outbound.ISupportTeamRepository;
import com.miasi.users.application.ports.outbound.IUserRepository;
import com.miasi.users.domain.model.AgentSnapshot;
import com.miasi.users.domain.model.AgentStatus;
import com.miasi.users.domain.model.SupportTeam;
import com.miasi.users.domain.model.SupportTeamID;
import com.miasi.users.domain.model.User;
import com.miasi.users.domain.model.UserID;
import java.util.List;

public class UserApplicationService
    implements ChangeAgentAvailabilityUseCase, AssignAgentToTeamUseCase, GetAvailableAgentsUseCase {

  private final IUserRepository userRepository;
  private final ISupportTeamRepository teamRepository;
  private final IEventPublisher eventPublisher;

  public UserApplicationService(
      IUserRepository userRepository,
      ISupportTeamRepository teamRepository,
      IEventPublisher eventPublisher) {
    this.userRepository = userRepository;
    this.teamRepository = teamRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public void changeAgentStatus(UserID agentId, AgentStatus newStatus) {
    User user =
        userRepository
            .findById(agentId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + agentId.id()));
    user.changeAgentStatus(newStatus);
    userRepository.save(user);
    user.popEvents().forEach(eventPublisher::publish);
  }

  @Override
  public void assignAgentToTeam(UserID agentId, SupportTeamID teamId) {
    userRepository
        .findById(agentId)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + agentId.id()));
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
  public List<AgentSnapshot> getAvailableAgentsForArea(String areaOfInterest) {
    List<SupportTeam> teams = teamRepository.findByAreaOfInterest(areaOfInterest);
    return userRepository.findAvailableAgents().stream()
        .filter(u -> u.getAgentStatus().isAvailable())
        .filter(u -> teams.stream().anyMatch(t -> t.hasMember(u.getId())))
        .map(
            u ->
                new AgentSnapshot(
                    u.getId().id(),
                    teams.stream()
                        .filter(t -> t.hasMember(u.getId()))
                        .findFirst()
                        .map(t -> t.getId().id())
                        .orElse(""),
                    true))
        .toList();
  }
}
