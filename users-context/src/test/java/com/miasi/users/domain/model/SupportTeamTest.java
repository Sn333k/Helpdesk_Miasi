package com.miasi.users.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.miasi.users.domain.events.AgentAssignedToTeam;
import com.miasi.users.domain.events.DomainEvent;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SupportTeamTest {

  private SupportTeam team;
  private UserID agent1;
  private UserID agent2;

  @BeforeEach
  void setUp() {
    team = new SupportTeam("Backend Team", "BACKEND");
    agent1 = new UserID("agent-1");
    agent2 = new UserID("agent-2");
  }

  // ─── Construction ─────────────────────────────────────────────────────────

  @Test
  void newTeam_shouldHaveCorrectNameAndArea() {
    assertThat(team.getName()).isEqualTo("Backend Team");
    assertThat(team.getAreaOfInterest()).isEqualTo("BACKEND");
    assertThat(team.getId()).isNotNull();
    assertThat(team.getAssignedAgents()).isEmpty();
  }

  @Test
  void newTeam_blankName_shouldThrow() {
    assertThatThrownBy(() -> new SupportTeam("  ", "IT"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("name");
  }

  @Test
  void newTeam_blankAreaOfInterest_shouldThrow() {
    assertThatThrownBy(() -> new SupportTeam("Team", "   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Area");
  }

  // ─── assignAgent ──────────────────────────────────────────────────────────

  @Test
  void assignAgent_shouldAddAgentToTeam() {
    team.assignAgent(agent1);

    assertThat(team.hasMember(agent1)).isTrue();
    assertThat(team.getAssignedAgents()).containsExactly(agent1);
  }

  @Test
  void assignAgent_shouldEmitAgentAssignedToTeamEvent() {
    team.assignAgent(agent1);
    List<DomainEvent> events = team.popEvents();

    assertThat(events).hasSize(1);
    assertThat(events.get(0)).isInstanceOf(AgentAssignedToTeam.class);
    AgentAssignedToTeam event = (AgentAssignedToTeam) events.get(0);
    assertThat(event.agentId()).isEqualTo(agent1);
    assertThat(event.teamId()).isEqualTo(team.getId());
  }

  @Test
  void assignAgent_null_shouldThrow() {
    assertThatThrownBy(() -> team.assignAgent(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("AgentId");
  }

  @Test
  void assignAgent_duplicate_shouldThrow() {
    team.assignAgent(agent1);

    assertThatThrownBy(() -> team.assignAgent(agent1))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("already assigned");
  }

  @Test
  void assignAgent_multipleAgents_allShouldBeMembersAnd() {
    team.assignAgent(agent1);
    team.assignAgent(agent2);

    assertThat(team.getAssignedAgents()).containsExactlyInAnyOrder(agent1, agent2);
  }

  // ─── removeAgent ──────────────────────────────────────────────────────────

  @Test
  void removeAgent_shouldRemoveFromTeam() {
    team.assignAgent(agent1);
    team.popEvents();

    team.removeAgent(agent1);

    assertThat(team.hasMember(agent1)).isFalse();
  }

  @Test
  void removeAgent_notMember_shouldThrow() {
    assertThatThrownBy(() -> team.removeAgent(agent1))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("not a member");
  }

  @Test
  void removeAgent_null_shouldThrow() {
    assertThatThrownBy(() -> team.removeAgent(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("AgentId");
  }

  // ─── supportsAreaOfInterest ───────────────────────────────────────────────

  @Test
  void supportsAreaOfInterest_matchingArea_returnsTrue() {
    assertThat(team.supportsAreaOfInterest("BACKEND")).isTrue();
  }

  @Test
  void supportsAreaOfInterest_caseInsensitiveMatch_returnsTrue() {
    assertThat(team.supportsAreaOfInterest("backend")).isTrue();
  }

  @Test
  void supportsAreaOfInterest_differentArea_returnsFalse() {
    assertThat(team.supportsAreaOfInterest("FRONTEND")).isFalse();
  }

  @Test
  void supportsAreaOfInterest_null_returnsFalse() {
    assertThat(team.supportsAreaOfInterest(null)).isFalse();
  }

  // ─── getAssignedAgents immutability ──────────────────────────────────────

  @Test
  void getAssignedAgents_shouldReturnImmutableView() {
    team.assignAgent(agent1);
    Set<UserID> view = team.getAssignedAgents();

    assertThatThrownBy(() -> view.add(agent2)).isInstanceOf(UnsupportedOperationException.class);
  }

  // ─── popEvents ────────────────────────────────────────────────────────────

  @Test
  void popEvents_shouldClearQueueAfterCall() {
    team.assignAgent(agent1);

    List<DomainEvent> first = team.popEvents();
    List<DomainEvent> second = team.popEvents();

    assertThat(first).isNotEmpty();
    assertThat(second).isEmpty();
  }
}
