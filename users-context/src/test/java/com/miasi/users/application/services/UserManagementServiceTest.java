package com.miasi.users.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.miasi.users.application.ports.outbound.IAssigneeRepository;
import com.miasi.users.application.ports.outbound.IEventPublisher;
import com.miasi.users.application.ports.outbound.IRequesterRepository;
import com.miasi.users.application.ports.outbound.ISupportTeamRepository;
import com.miasi.users.domain.events.DomainEvent;
import com.miasi.users.domain.model.AgentSnapshot;
import com.miasi.users.domain.model.AgentStatus;
import com.miasi.users.domain.model.Assignee;
import com.miasi.users.domain.model.EmailAddress;
import com.miasi.users.domain.model.Requester;
import com.miasi.users.domain.model.SupportTeam;
import com.miasi.users.domain.model.SupportTeamID;
import com.miasi.users.domain.model.UserID;
import com.miasi.users.domain.model.UserNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserManagementServiceTest {

  private FakeRequesterRepository requesterRepo;
  private FakeAssigneeRepository assigneeRepo;
  private FakeSupportTeamRepository teamRepo;
  private CapturingEventPublisher eventPublisher;
  private UserManagementService service;

  @BeforeEach
  void setUp() {
    requesterRepo = new FakeRequesterRepository();
    assigneeRepo = new FakeAssigneeRepository();
    teamRepo = new FakeSupportTeamRepository();
    eventPublisher = new CapturingEventPublisher();
    service = new UserManagementService(requesterRepo, assigneeRepo, teamRepo, eventPublisher);
  }

  @Test
  void createRequester_savesAndReturnsId() {
    UserID id = service.createRequester("alice@example.com");

    assertThat(id).isNotNull();
    assertThat(requesterRepo.findById(id)).isPresent();
  }

  @Test
  void createAssignee_withoutId_generatesId() {
    UserID id = service.createAssignee(null, AgentStatus.AVAILABLE);

    assertThat(id).isNotNull();
    assertThat(assigneeRepo.findById(id)).isPresent();
  }

  @Test
  void createAssignee_withId_usesProvidedId() {
    UserID id = service.createAssignee("my-custom-id", AgentStatus.BUSY);

    assertThat(id.id()).isEqualTo("my-custom-id");
    assertThat(assigneeRepo.findById(id)).isPresent();
  }

  @Test
  void changeEmail_updatesRequesterEmail() {
    UserID id = service.createRequester("old@example.com");

    service.changeEmail(id, new EmailAddress("new@example.com"));

    Requester requester = requesterRepo.findById(id).orElseThrow();
    assertThat(requester.getEmail().value()).isEqualTo("new@example.com");
  }

  @Test
  void changeEmail_throwsWhenRequesterNotFound() {
    assertThatThrownBy(
            () -> service.changeEmail(new UserID("missing"), new EmailAddress("a@example.com")))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void changeAgentStatus_updatesAssigneeStatus() {
    UserID id = service.createAssignee(null, AgentStatus.AVAILABLE);

    service.changeAgentStatus(id, AgentStatus.BUSY);

    Assignee assignee = assigneeRepo.findById(id).orElseThrow();
    assertThat(assignee.getAgentStatus()).isEqualTo(AgentStatus.BUSY);
  }

  @Test
  void changeAgentStatus_throwsWhenAssigneeNotFound() {
    assertThatThrownBy(() -> service.changeAgentStatus(new UserID("missing"), AgentStatus.BUSY))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void createTeam_savesAndReturnsId() {
    SupportTeamID teamId = service.createTeam("Alpha", "networking");

    assertThat(teamId).isNotNull();
    assertThat(teamRepo.findById(teamId)).isPresent();
  }

  @Test
  void assignAgentToTeam_addsAgentToTeam() {
    UserID agentId = service.createAssignee(null, AgentStatus.AVAILABLE);
    SupportTeamID teamId = service.createTeam("Beta", "security");

    service.assignAgentToTeam(agentId, teamId);

    SupportTeam team = teamRepo.findById(teamId).orElseThrow();
    assertThat(team.hasMember(agentId)).isTrue();
  }

  @Test
  void assignAgentToTeam_throwsWhenAgentNotFound() {
    SupportTeamID teamId = service.createTeam("Gamma", "hardware");

    assertThatThrownBy(() -> service.assignAgentToTeam(new UserID("missing"), teamId))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void removeAgentFromTeam_removesAgentFromTeam() {
    UserID agentId = service.createAssignee(null, AgentStatus.AVAILABLE);
    SupportTeamID teamId = service.createTeam("Delta", "software");
    service.assignAgentToTeam(agentId, teamId);

    service.removeAgentFromTeam(agentId, teamId);

    SupportTeam team = teamRepo.findById(teamId).orElseThrow();
    assertThat(team.hasMember(agentId)).isFalse();
  }

  @Test
  void getAvailableAgentsForArea_returnsSnapshots() {
    UserID agentId = service.createAssignee(null, AgentStatus.AVAILABLE);
    SupportTeamID teamId = service.createTeam("Epsilon", "general");
    service.assignAgentToTeam(agentId, teamId);
    assigneeRepo.markAvailableForArea(agentId, "general");

    List<AgentSnapshot> snapshots = service.getAvailableAgentsForArea("general");

    assertThat(snapshots).hasSize(1);
    assertThat(snapshots.get(0).agentId()).isEqualTo(agentId.id());
    assertThat(snapshots.get(0).available()).isTrue();
  }

  @Test
  void publishesEventsAfterCreateRequester() {
    service.createRequester("events@example.com");

    assertThat(eventPublisher.published).isEmpty();
  }

  // ─── Fakes ────────────────────────────────────────────────────────────────

  static class FakeRequesterRepository implements IRequesterRepository {
    private final Map<String, Requester> store = new HashMap<>();

    @Override
    public void save(Requester requester) {
      store.put(requester.getId().id(), requester);
    }

    @Override
    public Optional<Requester> findById(UserID id) {
      return Optional.ofNullable(store.get(id.id()));
    }
  }

  static class FakeAssigneeRepository implements IAssigneeRepository {
    private final Map<String, Assignee> store = new HashMap<>();
    private final Map<String, String> areaByAgentId = new HashMap<>();

    @Override
    public void save(Assignee assignee) {
      store.put(assignee.getId().id(), assignee);
    }

    @Override
    public Optional<Assignee> findById(UserID id) {
      return Optional.ofNullable(store.get(id.id()));
    }

    @Override
    public List<Assignee> findAvailableByArea(String area) {
      return store.values().stream()
          .filter(
              a ->
                  a.getAgentStatus() == AgentStatus.AVAILABLE
                      && area.equalsIgnoreCase(areaByAgentId.get(a.getId().id())))
          .toList();
    }

    void markAvailableForArea(UserID agentId, String area) {
      areaByAgentId.put(agentId.id(), area);
    }
  }

  static class FakeSupportTeamRepository implements ISupportTeamRepository {
    private final Map<String, SupportTeam> store = new HashMap<>();

    @Override
    public void save(SupportTeam team) {
      store.put(team.getId().id(), team);
    }

    @Override
    public Optional<SupportTeam> findById(SupportTeamID id) {
      return Optional.ofNullable(store.get(id.id()));
    }

    @Override
    public List<SupportTeam> findByAreaOfInterest(String area) {
      return store.values().stream()
          .filter(t -> t.getAreaOfInterest().equalsIgnoreCase(area))
          .toList();
    }
  }

  static class CapturingEventPublisher implements IEventPublisher {
    final List<DomainEvent> published = new ArrayList<>();

    @Override
    public void publish(DomainEvent event) {
      published.add(event);
    }
  }
}
