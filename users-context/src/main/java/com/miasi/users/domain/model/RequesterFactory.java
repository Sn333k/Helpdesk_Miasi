package com.miasi.users.domain.model;

public final class RequesterFactory {

  private RequesterFactory() {}

  public static Requester create(EmailAddress email) {
    return new Requester(email);
  }
}
