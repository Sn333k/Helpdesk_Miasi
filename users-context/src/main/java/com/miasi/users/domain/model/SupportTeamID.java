package com.miasi.users.domain.model;

import java.util.Objects;

public record SupportTeamID(String id) {
  public SupportTeamID {
    if (id == null || id.isBlank())
      throw new IllegalArgumentException("SupportTeamID must not be blank");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SupportTeamID other)) return false;
    return Objects.equals(id, other.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
