package com.miasi.users.application.ports.inbound;

import com.miasi.users.domain.model.SupportTeamID;
import com.miasi.users.domain.model.UserID;

public interface AssignAgentToTeamUseCase {
  void assignAgentToTeam(UserID agentId, SupportTeamID teamId);
}
