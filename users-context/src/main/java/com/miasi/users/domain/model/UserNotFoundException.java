package com.miasi.users.domain.model;

public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException(UserID id) {
    super("User not found: " + id.id());
  }
}
