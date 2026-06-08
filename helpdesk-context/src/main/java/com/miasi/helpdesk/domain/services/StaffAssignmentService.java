package com.miasi.helpdesk.domain.services;

import com.miasi.helpdesk.domain.model.AgentSnapshot;
import com.miasi.helpdesk.domain.model.Category;
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
