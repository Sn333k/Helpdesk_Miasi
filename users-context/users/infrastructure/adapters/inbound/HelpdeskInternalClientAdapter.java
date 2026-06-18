package com.miasi.users.infrastructure.adapters.inbound;

import com.miasi.users.application.services.AgentSnapshotDto;
import com.miasi.users.application.services.UserApplicationService;

import java.util.List;
import java.util.Map;

/**
 * Inbound adapter: HelpdeskInternalClientAdapter (Open Host Service)
 *
 * Exposes internal HTTP endpoints consumed by the Ticket bounded context.
 * This is the OHS boundary: it translates between the Users domain model
 * and a shared protocol (AgentSnapshotDto) that the Ticket context can use
 * through its own ACL (AgentAvailabilityAdapter).
 *
 * Route:
 *   GET /internal/users/agents?specialization={category}
 *       -> Returns list of AgentSnapshotDto for agents matching the category
 */
public class HelpdeskInternalClientAdapter {

    private final UserApplicationService userApplicationService;

    public HelpdeskInternalClientAdapter(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    /**
     * GET /internal/users/agents?specialization={category}
     *
     * Returns agent snapshots for all agents whose SupportTeam covers
     * the requested specialization/category.
     *
     * @param category the ticket category (maps to AreaOfInterest)
     * @return HTTP-style response with list of agent snapshots
     */
    public UserRestController.ResponseEntity getAgentsForSpecialization(String category) {
        if (category == null || category.isBlank()) {
            return UserRestController.ResponseEntity.badRequest(
                    "Query parameter 'specialization' is required");
        }

        try {
            List<AgentSnapshotDto> agents =
                    userApplicationService.getAvailableAgentsForCategory(category);

            List<Map<String, Object>> responseBody = agents.stream()
                    .map(dto -> (Map<String, Object>) Map.of(
                            "agentId", dto.agentId(),
                            "email", dto.email(),
                            "currentLoad", dto.currentLoad(),
                            "available", dto.available()))
                    .toList();

            return UserRestController.ResponseEntity.ok(responseBody);
        } catch (IllegalArgumentException e) {
            return UserRestController.ResponseEntity.badRequest(e.getMessage());
        }
    }
}
