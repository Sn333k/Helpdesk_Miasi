package com.miasi.users.application.ports.inbound;

import com.miasi.users.domain.model.SupportTeamID;

public interface CreateSupportTeamUseCase {
  SupportTeamID createTeam(String name, String areaOfInterest);
}
