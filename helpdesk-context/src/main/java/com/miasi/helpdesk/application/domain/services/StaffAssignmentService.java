package com.miasi.helpdesk.application.domain.services;

import com.miasi.helpdesk.application.domain.model.AgentSnapshot;
import com.miasi.helpdesk.application.domain.model.Category;
import java.util.Comparator;
import java.util.List;

public class StaffAssignmentService {

  public AgentSnapshot findOptimalAgent(Category category, List<AgentSnapshot> candidates) {
    return candidates.stream()
        .filter(AgentSnapshot::available)
        .min(Comparator.comparingInt(AgentSnapshot::currentLoad))
        .orElseThrow(() -> new NoAvailableAgentException(category));
  }
}
