package com.miasi.users.application.ports.inbound;

import com.miasi.users.domain.model.AgentStatus;
import com.miasi.users.domain.model.UserID;

public interface CreateAssigneeUseCase {
  /** Pass null for agentId to auto-generate a UUID. */
  UserID createAssignee(String agentId, AgentStatus initialStatus);
}
