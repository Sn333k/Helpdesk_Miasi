package com.miasi.users.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.miasi.users.domain.events.AgentAvailabilityChanged;
import com.miasi.users.domain.events.DomainEvent;
import com.miasi.users.domain.events.UserDeactivated;
import java.util.List;
import org.junit.jupiter.api.Test;

class UserTest {

  private static final EmailAddress EMAIL = new EmailAddress("test@example.com");

  private User newUser() {
    return new User(EMAIL);
  }

  // ─── Construction ─────────────────────────────────────────────────────────

  @Test
  void newUser_shouldBeActiveAndOffline() {
    User user = newUser();

    assertThat(user.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
    assertThat(user.getAgentStatus()).isEqualTo(AgentStatus.OFFLINE);
    assertThat(user.getId()).isNotNull();
  }

  @Test
  void newUser_nullEmail_shouldThrow() {
    assertThatThrownBy(() -> new User(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Email");
  }

  // ─── changeEmail ──────────────────────────────────────────────────────────

  @Test
  void changeEmail_onActiveAccount_shouldSucceed() {
    User user = newUser();
    EmailAddress newEmail = new EmailAddress("new@example.com");

    user.changeEmail(newEmail);

    assertThat(user.getEmail()).isEqualTo(newEmail);
  }

  @Test
  void changeEmail_null_shouldThrow() {
    User user = newUser();

    assertThatThrownBy(() -> user.changeEmail(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("email");
  }

  @Test
  void changeEmail_onInactiveAccount_shouldThrow() {
    User user = newUser();
    user.deactivate();

    assertThatThrownBy(() -> user.changeEmail(new EmailAddress("x@y.com")))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("inactive");
  }

  // ─── deactivate ───────────────────────────────────────────────────────────

  @Test
  void deactivate_shouldSetInactiveAndOffline() {
    User user = newUser();

    user.deactivate();

    assertThat(user.getAccountStatus()).isEqualTo(AccountStatus.INACTIVE);
    assertThat(user.getAgentStatus()).isEqualTo(AgentStatus.OFFLINE);
  }

  @Test
  void deactivate_shouldEmitUserDeactivatedEvent() {
    User user = newUser();

    user.deactivate();
    List<DomainEvent> events = user.popEvents();

    assertThat(events).hasSize(1);
    assertThat(events.get(0)).isInstanceOf(UserDeactivated.class);
  }

  @Test
  void deactivate_alreadyInactive_shouldThrow() {
    User user = newUser();
    user.deactivate();
    user.popEvents();

    assertThatThrownBy(user::deactivate)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("ACTIVE");
  }

  // ─── changeAgentStatus ────────────────────────────────────────────────────

  @Test
  void changeAgentStatus_toAvailable_shouldSucceed() {
    User user = newUser();

    user.changeAgentStatus(AgentStatus.AVAILABLE);

    assertThat(user.getAgentStatus()).isEqualTo(AgentStatus.AVAILABLE);
  }

  @Test
  void changeAgentStatus_shouldEmitAvailabilityChangedEvent() {
    User user = newUser();

    user.changeAgentStatus(AgentStatus.AVAILABLE);
    List<DomainEvent> events = user.popEvents();

    assertThat(events).hasSize(1);
    assertThat(events.get(0)).isInstanceOf(AgentAvailabilityChanged.class);
    AgentAvailabilityChanged event = (AgentAvailabilityChanged) events.get(0);
    assertThat(event.newStatus()).isEqualTo(AgentStatus.AVAILABLE);
    assertThat(event.userId()).isEqualTo(user.getId());
  }

  @Test
  void changeAgentStatus_toSameStatus_shouldThrow() {
    User user = newUser(); // starts OFFLINE

    assertThatThrownBy(() -> user.changeAgentStatus(AgentStatus.OFFLINE))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("already");
  }

  @Test
  void changeAgentStatus_onInactiveAccount_shouldThrow() {
    User user = newUser();
    user.deactivate();

    assertThatThrownBy(() -> user.changeAgentStatus(AgentStatus.AVAILABLE))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Inactive");
  }

  @Test
  void changeAgentStatus_null_shouldThrow() {
    User user = newUser();

    assertThatThrownBy(() -> user.changeAgentStatus(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("status");
  }

  // ─── popEvents ────────────────────────────────────────────────────────────

  @Test
  void popEvents_shouldClearEventsAfterCall() {
    User user = newUser();
    user.changeAgentStatus(AgentStatus.AVAILABLE);

    List<DomainEvent> first = user.popEvents();
    List<DomainEvent> second = user.popEvents();

    assertThat(first).isNotEmpty();
    assertThat(second).isEmpty();
  }
}
