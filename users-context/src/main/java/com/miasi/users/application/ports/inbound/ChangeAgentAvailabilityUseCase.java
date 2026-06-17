package com.miasi.users.application.ports.inbound;

import com.miasi.users.domain.model.AgentStatus;
import com.miasi.users.domain.model.UserID;

public interface ChangeAgentAvailabilityUseCase {
  void changeAgentStatus(UserID agentId, AgentStatus newStatus);
}
