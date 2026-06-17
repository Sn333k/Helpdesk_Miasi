package com.miasi.users.application.ports.inbound;

import com.miasi.users.domain.model.AgentSnapshot;
import java.util.List;

public interface GetAvailableAgentsUseCase {
  List<AgentSnapshot> getAvailableAgentsForArea(String areaOfInterest);
}
