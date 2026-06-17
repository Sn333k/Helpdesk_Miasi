package com.miasi.users.domain.model;

import java.util.Objects;

public record UserID(String id) {
  public UserID {
    if (id == null || id.isBlank()) throw new IllegalArgumentException("UserID must not be blank");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UserID other)) return false;
    return Objects.equals(id, other.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
