package com.miasi.users.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class UserIDTest {

  @Test
  void validId_shouldBeAccepted() {
    UserID id = new UserID("user-123");
    assertThat(id.id()).isEqualTo("user-123");
  }

  @Test
  void nullId_shouldThrow() {
    assertThatThrownBy(() -> new UserID(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("UserID");
  }

  @Test
  void blankId_shouldThrow() {
    assertThatThrownBy(() -> new UserID("   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("UserID");
  }

  @Test
  void twoIdsWithSameValue_shouldBeEqual() {
    assertThat(new UserID("abc")).isEqualTo(new UserID("abc"));
  }

  @Test
  void twoIdsWithDifferentValues_shouldNotBeEqual() {
    assertThat(new UserID("abc")).isNotEqualTo(new UserID("xyz"));
  }
}
