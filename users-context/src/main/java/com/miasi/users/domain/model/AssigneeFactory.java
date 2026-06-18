package com.miasi.users.domain.model;

public final class AssigneeFactory {

  private AssigneeFactory() {}

  public static Assignee create(AgentStatus initialStatus) {
    return new Assignee(initialStatus);
  }

  public static Assignee create(UserID id, AgentStatus initialStatus) {
    return new Assignee(id, initialStatus);
  }
}
