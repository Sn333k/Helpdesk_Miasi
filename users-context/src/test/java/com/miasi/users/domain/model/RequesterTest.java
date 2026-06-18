package com.miasi.users.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.miasi.users.domain.events.DomainEvent;
import com.miasi.users.domain.events.RequesterDeactivated;
import java.util.List;
import org.junit.jupiter.api.Test;

class RequesterTest {

  @Test
  void create_setsActiveStatusAndEmail() {
    Requester requester = RequesterFactory.create(new EmailAddress("alice@example.com"));

    assertThat(requester.getId()).isNotNull();
    assertThat(requester.getEmail().value()).isEqualTo("alice@example.com");
    assertThat(requester.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
  }

  @Test
  void changeEmail_updatesEmail() {
    Requester requester = RequesterFactory.create(new EmailAddress("old@example.com"));

    requester.changeEmail(new EmailAddress("new@example.com"));

    assertThat(requester.getEmail().value()).isEqualTo("new@example.com");
  }

  @Test
  void changeEmail_throwsWhenInactive() {
    Requester requester = RequesterFactory.create(new EmailAddress("x@example.com"));
    requester.deactivate();
    requester.popEvents();

    assertThatThrownBy(() -> requester.changeEmail(new EmailAddress("y@example.com")))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void deactivate_setsInactiveAndEmitsEvent() {
    Requester requester = RequesterFactory.create(new EmailAddress("a@example.com"));

    requester.deactivate();
    List<DomainEvent> events = requester.popEvents();

    assertThat(requester.getAccountStatus()).isEqualTo(AccountStatus.INACTIVE);
    assertThat(events).hasSize(1);
    assertThat(events.get(0)).isInstanceOf(RequesterDeactivated.class);
  }

  @Test
  void deactivate_throwsWhenAlreadyInactive() {
    Requester requester = RequesterFactory.create(new EmailAddress("b@example.com"));
    requester.deactivate();
    requester.popEvents();

    assertThatThrownBy(requester::deactivate).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void popEvents_clearsEventList() {
    Requester requester = RequesterFactory.create(new EmailAddress("c@example.com"));
    requester.deactivate();

    requester.popEvents();
    List<DomainEvent> second = requester.popEvents();

    assertThat(second).isEmpty();
  }
}
