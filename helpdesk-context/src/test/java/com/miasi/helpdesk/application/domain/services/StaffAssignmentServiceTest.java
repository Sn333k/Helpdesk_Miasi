package com.miasi.helpdesk.application.domain.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.miasi.helpdesk.application.domain.model.AgentSnapshot;
import com.miasi.helpdesk.application.domain.model.Category;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StaffAssignmentServiceTest {

  private StaffAssignmentService service;
  private Category category;

  @BeforeEach
  void setUp() {
    service = new StaffAssignmentService();
    category = new Category("IT");
  }

  @Test
  void findOptimalAgent_shouldReturnLeastLoadedAvailableAgent() {
    AgentSnapshot busy = new AgentSnapshot("agent-busy", 10, true);
    AgentSnapshot light = new AgentSnapshot("agent-light", 2, true);
    AgentSnapshot medium = new AgentSnapshot("agent-medium", 5, true);

    AgentSnapshot result = service.findOptimalAgent(category, List.of(busy, light, medium));

    assertThat(result.agentId()).isEqualTo("agent-light");
  }

  @Test
  void findOptimalAgent_withEmptyList_shouldThrow() {
    assertThatThrownBy(() -> service.findOptimalAgent(category, List.of()))
        .isInstanceOf(NoAvailableAgentException.class);
  }

  @Test
  void findOptimalAgent_skipsUnavailableAgents() {
    AgentSnapshot unavailable = new AgentSnapshot("agent-off", 0, false);
    AgentSnapshot available = new AgentSnapshot("agent-on", 5, true);

    AgentSnapshot result = service.findOptimalAgent(category, List.of(unavailable, available));

    assertThat(result.agentId()).isEqualTo("agent-on");
  }

  @Test
  void findOptimalAgent_allUnavailable_shouldThrow() {
    AgentSnapshot a = new AgentSnapshot("agent-a", 0, false);
    AgentSnapshot b = new AgentSnapshot("agent-b", 1, false);

    assertThatThrownBy(() -> service.findOptimalAgent(category, List.of(a, b)))
        .isInstanceOf(NoAvailableAgentException.class);
  }

  @Test
  void findOptimalAgent_singleAvailableAgent_returnsThatAgent() {
    AgentSnapshot only = new AgentSnapshot("agent-only", 3, true);

    AgentSnapshot result = service.findOptimalAgent(category, List.of(only));

    assertThat(result.agentId()).isEqualTo("agent-only");
  }

  @Test
  void findOptimalAgent_tieInLoad_returnsFirstByStreamOrder() {
    // Both have the same load – min() takes the first one encountered
    AgentSnapshot first = new AgentSnapshot("agent-first", 3, true);
    AgentSnapshot second = new AgentSnapshot("agent-second", 3, true);

    AgentSnapshot result = service.findOptimalAgent(category, List.of(first, second));

    assertThat(result.agentId()).isEqualTo("agent-first");
  }
}
