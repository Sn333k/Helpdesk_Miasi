package com.miasi.helpdesk.application.domain.model;

public record Category(String name) {
  public Category {
    if (name == null || name.isBlank())
      throw new IllegalArgumentException("Category name must not be blank");
  }
}
