package com.miasi.users.domain.model;

public record EmailAddress(String value) {

  private static final String EMAIL_REGEX = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

  public EmailAddress {
    if (value == null || value.isBlank())
      throw new IllegalArgumentException("Email address must not be blank");
    if (!value.matches(EMAIL_REGEX))
      throw new IllegalArgumentException("Invalid email address format: " + value);
  }
}
