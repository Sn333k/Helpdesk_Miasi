package com.miasi.helpdesk.domain.model;

public record RequesterID(String id) {
  public RequesterID {
    if (id == null || id.isBlank())
      throw new IllegalArgumentException("RequesterID must not be blank");
  }
}
