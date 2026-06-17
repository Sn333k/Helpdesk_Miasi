package com.miasi.users.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.miasi.users.application.ports.outbound.IEventPublisher;
import com.miasi.users.application.ports.outbound.ISupportTeamRepository;
import com.miasi.users.application.ports.outbound.IUserRepository;
import com.miasi.users.domain.events.AgentAvailabilityChanged;
import com.miasi.users.domain.events.DomainEvent;
import com.miasi.users.domain.model.AgentSnapshot;
import com.miasi.users.domain.model.AgentStatus;
import com.miasi.users.domain.model.EmailAddress;
import com.miasi.users.domain.model.SupportTeam;
import com.miasi.users.domain.model.SupportTeamID;
import com.miasi.users.domain.model.User;
import com.miasi.users.domain.model.UserID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserApplicationServiceTest {

  private FakeUserRepository userRepo;
  private FakeSupportTeamRepository teamRepo;
  private CapturingEventPublisher publisher;
  private UserApplicationService service;

  @BeforeEach
  void setUp() {
    userRepo = new FakeUserRepository();
    teamRepo = new FakeSupportTeamRepository();
    publisher = new CapturingEventPublisher();
    service = new UserApplicationService(userRepo, teamRepo, publisher);
  }

  // ─── changeAgentStatus ────────────────────────────────────────────────────

  @Test
  void changeAgentStatus_shouldUpdateUserAndPublishEvent() {
    User user = new User(new EmailAddress("a@b.com"));
    userRepo.save(user);

    service.changeAgentStatus(user.getId(), AgentStatus.AVAILABLE);

    User updated = userRepo.findById(user.getId()).orElseThrow();
    assertThat(updated.getAgentStatus()).isEqualTo(AgentStatus.AVAILABLE);
    assertThat(publisher.getPublished())
        .hasSize(1)
        .first()
        .isInstanceOf(AgentAvailabilityChanged.class);
  }

  @Test
  void changeAgentStatus_userNotFound_shouldThrow() {
    UserID missing = new UserID("no-such-user");

    assertThatThrownBy(() -> service.changeAgentStatus(missing, AgentStatus.AVAILABLE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("not found");
  }

  @Test
  void changeAgentStatus_toSameStatus_shouldThrow() {
    User user = new User(new EmailAddress("a@b.com")); // starts OFFLINE
    userRepo.save(user);

    assertThatThrownBy(() -> service.changeAgentStatus(user.getId(), AgentStatus.OFFLINE))
        .isInstanceOf(IllegalStateException.class);
  }

  // ─── assignAgentToTeam ────────────────────────────────────────────────────

  @Test
  void assignAgentToTeam_shouldAddAgentToTeamAndPublishEvent() {
    User user = new User(new EmailAddress("agent@b.com"));
    userRepo.save(user);
    SupportTeam team = new SupportTeam("IT Team", "IT");
    teamRepo.save(team);

    service.assignAgentToTeam(user.getId(), team.getId());

    SupportTeam updated = teamRepo.findById(team.getId()).orElseThrow();
    assertThat(updated.hasMember(user.getId())).isTrue();
    assertThat(publisher.getPublished()).hasSize(1);
  }

  @Test
  void assignAgentToTeam_userNotFound_shouldThrow() {
    SupportTeam team = new SupportTeam("IT Team", "IT");
    teamRepo.save(team);

    assertThatThrownBy(() -> service.assignAgentToTeam(new UserID("ghost"), team.getId()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("User not found");
  }

  @Test
  void assignAgentToTeam_teamNotFound_shouldThrow() {
    User user = new User(new EmailAddress("a@b.com"));
    userRepo.save(user);

    assertThatThrownBy(
            () -> service.assignAgentToTeam(user.getId(), new SupportTeamID("ghost-team")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("SupportTeam not found");
  }

  // ─── getAvailableAgentsForArea ────────────────────────────────────────────

  @Test
  void getAvailableAgentsForArea_shouldReturnOnlyAvailableAgentsInMatchingTeams() {
    User available = new User(new EmailAddress("avail@b.com"));
    available.changeAgentStatus(AgentStatus.AVAILABLE);
    userRepo.save(available);

    User offline = new User(new EmailAddress("off@b.com"));
    userRepo.save(offline); // stays OFFLINE

    SupportTeam team = new SupportTeam("Backend Team", "BACKEND");
    team.assignAgent(available.getId());
    team.assignAgent(offline.getId());
    teamRepo.save(team);

    List<AgentSnapshot> result = service.getAvailableAgentsForArea("BACKEND");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).agentId()).isEqualTo(available.getId().id());
    assertThat(result.get(0).available()).isTrue();
  }

  @Test
  void getAvailableAgentsForArea_noMatchingTeam_shouldReturnEmpty() {
    User available = new User(new EmailAddress("avail@b.com"));
    available.changeAgentStatus(AgentStatus.AVAILABLE);
    userRepo.save(available);
    // No team for area "FRONTEND"

    List<AgentSnapshot> result = service.getAvailableAgentsForArea("FRONTEND");

    assertThat(result).isEmpty();
  }

  // ─── Fakes ────────────────────────────────────────────────────────────────

  private static class FakeUserRepository implements IUserRepository {
    private final Map<String, User> store = new HashMap<>();

    @Override
    public void save(User user) {
      store.put(user.getId().id(), user);
    }

    @Override
    public Optional<User> findById(UserID id) {
      return Optional.ofNullable(store.get(id.id()));
    }

    @Override
    public List<User> findAvailableAgents() {
      return store.values().stream().filter(u -> u.getAgentStatus().isAvailable()).toList();
    }
  }

  private static class FakeSupportTeamRepository implements ISupportTeamRepository {
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
      return store.values().stream().filter(t -> t.supportsAreaOfInterest(area)).toList();
    }
  }

  private static class CapturingEventPublisher implements IEventPublisher {
    private final List<DomainEvent> published = new ArrayList<>();

    @Override
    public void publish(DomainEvent event) {
      published.add(event);
    }

    List<DomainEvent> getPublished() {
      return List.copyOf(published);
    }
  }
}
