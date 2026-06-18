package com.miasi.users.infrastructure.persistence;

import com.miasi.users.domain.model.AccountStatus;
import com.miasi.users.domain.model.AgentStatus;
import com.miasi.users.domain.model.Assignee;
import com.miasi.users.domain.model.UserID;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "assignees")
public class AssigneeEntity {

  @Id private String id;
  private String agentStatus;
  private String accountStatus;

  public AssigneeEntity() {}

  public AssigneeEntity(Assignee assignee) {
    this.id = assignee.getId().id();
    this.agentStatus = assignee.getAgentStatus().name();
    this.accountStatus = assignee.getAccountStatus().name();
  }

  public Assignee toDomain() {
    return Assignee.reconstitute(
        new UserID(id), AgentStatus.valueOf(agentStatus), AccountStatus.valueOf(accountStatus));
  }
}
