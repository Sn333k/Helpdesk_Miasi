package com.miasi.users.application.ports.inbound;

import com.miasi.users.domain.model.SupportTeamID;
import com.miasi.users.domain.model.UserID;

public interface RemoveAgentFromTeamUseCase {
  void removeAgentFromTeam(UserID agentId, SupportTeamID teamId);
}
