package com.miasi.users.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EmailAddressTest {

  @Test
  void validEmail_shouldBeAccepted() {
    EmailAddress email = new EmailAddress("jan.kowalski@example.com");
    assertThat(email.value()).isEqualTo("jan.kowalski@example.com");
  }

  @Test
  void nullEmail_shouldThrow() {
    assertThatThrownBy(() -> new EmailAddress(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("blank");
  }

  @Test
  void blankEmail_shouldThrow() {
    assertThatThrownBy(() -> new EmailAddress("   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("blank");
  }

  @Test
  void emailWithoutAtSign_shouldThrow() {
    assertThatThrownBy(() -> new EmailAddress("notanemail.com"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid email");
  }

  @Test
  void emailWithoutDomain_shouldThrow() {
    assertThatThrownBy(() -> new EmailAddress("user@"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void twoEmailsWithSameValue_shouldBeEqual() {
    EmailAddress a = new EmailAddress("a@b.com");
    EmailAddress b = new EmailAddress("a@b.com");
    assertThat(a).isEqualTo(b);
  }
}
