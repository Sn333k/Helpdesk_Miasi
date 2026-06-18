package com.miasi.users.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.miasi.users.domain.events.AgentAvailabilityChanged;
import com.miasi.users.domain.events.AssigneeDeactivated;
import com.miasi.users.domain.events.DomainEvent;
import java.util.List;
import org.junit.jupiter.api.Test;

class AssigneeTest {

  @Test
  void create_withoutId_generatesUuid() {
    Assignee assignee = AssigneeFactory.create(AgentStatus.AVAILABLE);

    assertThat(assignee.getId()).isNotNull();
    assertThat(assignee.getId().id()).isNotBlank();
    assertThat(assignee.getAgentStatus()).isEqualTo(AgentStatus.AVAILABLE);
    assertThat(assignee.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
  }

  @Test
  void create_withId_usesProvidedId() {
    UserID id = new UserID("custom-id");
    Assignee assignee = AssigneeFactory.create(id, AgentStatus.BUSY);

    assertThat(assignee.getId()).isEqualTo(id);
    assertThat(assignee.getAgentStatus()).isEqualTo(AgentStatus.BUSY);
  }

  @Test
  void changeAgentStatus_updatesStatusAndEmitsEvent() {
    Assignee assignee = AssigneeFactory.create(AgentStatus.AVAILABLE);

    assignee.changeAgentStatus(AgentStatus.BUSY);
    List<DomainEvent> events = assignee.popEvents();

    assertThat(assignee.getAgentStatus()).isEqualTo(AgentStatus.BUSY);
    assertThat(events).hasSize(1);
    assertThat(events.get(0)).isInstanceOf(AgentAvailabilityChanged.class);
  }

  @Test
  void changeAgentStatus_throwsOnSameStatus() {
    Assignee assignee = AssigneeFactory.create(AgentStatus.AVAILABLE);

    assertThatThrownBy(() -> assignee.changeAgentStatus(AgentStatus.AVAILABLE))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void changeAgentStatus_throwsWhenInactive() {
    Assignee assignee = AssigneeFactory.create(AgentStatus.AVAILABLE);
    assignee.deactivate();
    assignee.popEvents();

    assertThatThrownBy(() -> assignee.changeAgentStatus(AgentStatus.BUSY))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void deactivate_setsInactiveAndEmitsTwoEvents() {
    Assignee assignee = AssigneeFactory.create(AgentStatus.AVAILABLE);

    assignee.deactivate();
    List<DomainEvent> events = assignee.popEvents();

    assertThat(assignee.getAccountStatus()).isEqualTo(AccountStatus.INACTIVE);
    assertThat(assignee.getAgentStatus()).isEqualTo(AgentStatus.UNAVAILABLE);
    assertThat(events).hasSize(2);
    assertThat(events).anyMatch(e -> e instanceof AssigneeDeactivated);
    assertThat(events).anyMatch(e -> e instanceof AgentAvailabilityChanged);
  }

  @Test
  void deactivate_throwsWhenAlreadyInactive() {
    Assignee assignee = AssigneeFactory.create(AgentStatus.AVAILABLE);
    assignee.deactivate();
    assignee.popEvents();

    assertThatThrownBy(assignee::deactivate).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void suspend_setsStatusAndEmitsEvent() {
    Assignee assignee = AssigneeFactory.create(AgentStatus.AVAILABLE);

    assignee.suspend();
    List<DomainEvent> events = assignee.popEvents();

    assertThat(assignee.getAccountStatus()).isEqualTo(AccountStatus.SUSPENDED);
    assertThat(assignee.getAgentStatus()).isEqualTo(AgentStatus.UNAVAILABLE);
    assertThat(events).hasSize(1);
    assertThat(events.get(0)).isInstanceOf(AgentAvailabilityChanged.class);
  }

  @Test
  void suspend_throwsWhenAlreadySuspended() {
    Assignee assignee = AssigneeFactory.create(AgentStatus.AVAILABLE);
    assignee.suspend();
    assignee.popEvents();

    assertThatThrownBy(assignee::suspend).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void popEvents_clearsEventList() {
    Assignee assignee = AssigneeFactory.create(AgentStatus.AVAILABLE);
    assignee.changeAgentStatus(AgentStatus.BUSY);

    assignee.popEvents();
    List<DomainEvent> second = assignee.popEvents();

    assertThat(second).isEmpty();
  }
}
