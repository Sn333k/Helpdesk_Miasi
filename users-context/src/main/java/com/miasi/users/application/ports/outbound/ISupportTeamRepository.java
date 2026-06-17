package com.miasi.users.application.ports.outbound;

import com.miasi.users.domain.model.SupportTeam;
import com.miasi.users.domain.model.SupportTeamID;
import java.util.List;
import java.util.Optional;

public interface ISupportTeamRepository {
  void save(SupportTeam team);

  Optional<SupportTeam> findById(SupportTeamID id);

  List<SupportTeam> findByAreaOfInterest(String area);
}
