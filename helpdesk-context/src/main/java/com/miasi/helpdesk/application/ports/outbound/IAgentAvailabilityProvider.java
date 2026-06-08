package com.miasi.helpdesk.application.ports.outbound;

import com.miasi.helpdesk.domain.model.AgentSnapshot;
import com.miasi.helpdesk.domain.model.Category;
import java.util.List;

public interface IAgentAvailabilityProvider {
  List<AgentSnapshot> getAvailableAgentsForCategory(Category category);
}
