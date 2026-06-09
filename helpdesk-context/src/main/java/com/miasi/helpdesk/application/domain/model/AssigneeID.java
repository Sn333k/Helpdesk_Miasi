package com.miasi.helpdesk.application.domain.model;

public record AssigneeID(String id) {
  public AssigneeID {
    if (id == null || id.isBlank())
      throw new IllegalArgumentException("AssigneeID must not be blank");
  }
}
