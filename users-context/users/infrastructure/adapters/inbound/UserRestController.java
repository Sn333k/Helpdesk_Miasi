package com.miasi.users.infrastructure.adapters.inbound;

import com.miasi.users.application.ports.inbound.ChangeAgentAvailabilityUseCase;
import com.miasi.users.application.ports.inbound.ManageAssigneeUseCase;
import com.miasi.users.domain.model.AgentStatus;

import java.util.Map;

/**
 * Inbound adapter: UserRestController
 *
 * Maps incoming HTTP requests to use-case commands.
 * Designed as a plain Java class; wire into your HTTP server
 * (e.g. com.sun.net.httpserver.HttpServer) in the composition root.
 *
 * Routes handled:
 *   POST /api/users/assignees              -> createAssignee
 *   POST /api/users/requesters             -> createRequester
 *   POST /api/users/teams                  -> createSupportTeam
 *   PUT  /api/users/{id}/availability      -> changeAvailability
 *   POST /api/users/{id}/suspend           -> suspendAgent
 *   POST /api/users/{id}/activate          -> activateAgent
 *   POST /api/users/{id}/department        -> assignAgentToDepartment
 *   DELETE /api/users/{id}/department/{deptId} -> removeAgentFromDepartment
 */
public class UserRestController {

    private final ManageAssigneeUseCase manageAssigneeUseCase;
    private final ChangeAgentAvailabilityUseCase changeAgentAvailabilityUseCase;

    public UserRestController(ManageAssigneeUseCase manageAssigneeUseCase,
                               ChangeAgentAvailabilityUseCase changeAgentAvailabilityUseCase) {
        this.manageAssigneeUseCase = manageAssigneeUseCase;
        this.changeAgentAvailabilityUseCase = changeAgentAvailabilityUseCase;
    }

    /**
     * POST /api/users/assignees
     * Body params: { "email": "..." }
     *
     * @return generated userId
     */
    public ResponseEntity postNewAssignee(Map<String, String> body) {
        try {
            String email = requireField(body, "email");
            String userId = manageAssigneeUseCase.createAssignee(email);
            return ResponseEntity.ok(Map.of("userId", userId));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest(e.getMessage());
        }
    }

    /**
     * POST /api/users/requesters
     * Body params: { "email": "..." }
     *
     * @return generated userId
     */
    public ResponseEntity postNewRequester(Map<String, String> body) {
        try {
            String email = requireField(body, "email");
            String userId = manageAssigneeUseCase.createRequester(email);
            return ResponseEntity.ok(Map.of("userId", userId));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest(e.getMessage());
        }
    }

    /**
     * POST /api/users/teams
     * Body params: { "name": "...", "areas": "networking,hardware" }
     *
     * @return generated teamId
     */
    public ResponseEntity postNewSupportTeam(Map<String, String> body) {
        try {
            String name = requireField(body, "name");
            String areas = requireField(body, "areas");
            String teamId = manageAssigneeUseCase.createSupportTeam(name, areas);
            return ResponseEntity.ok(Map.of("teamId", teamId));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest(e.getMessage());
        }
    }

    /**
     * PUT /api/users/{id}/availability
     * Body params: { "status": "AVAILABLE|BUSY|UNAVAILABLE" }
     */
    public ResponseEntity putAgentAvailability(String agentId, Map<String, String> body) {
        try {
            String statusStr = requireField(body, "status");
            AgentStatus newStatus = parseAgentStatus(statusStr);
            changeAgentAvailabilityUseCase.changeAvailability(agentId, newStatus);
            return ResponseEntity.ok(Map.of("message", "Availability updated"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest(e.getMessage());
        }
    }

    /**
     * POST /api/users/{id}/suspend
     */
    public ResponseEntity postSuspendAgent(String agentId) {
        try {
            manageAssigneeUseCase.suspendAgent(agentId);
            return ResponseEntity.ok(Map.of("message", "Agent suspended"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest(e.getMessage());
        }
    }

    /**
     * POST /api/users/{id}/activate
     */
    public ResponseEntity postActivateAgent(String agentId) {
        try {
            manageAssigneeUseCase.activateAgent(agentId);
            return ResponseEntity.ok(Map.of("message", "Agent activated"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest(e.getMessage());
        }
    }

    /**
     * POST /api/users/{id}/department
     * Body params: { "departmentId": "..." }
     */
    public ResponseEntity postAssignToDepartment(String agentId, Map<String, String> body) {
        try {
            String departmentId = requireField(body, "departmentId");
            manageAssigneeUseCase.assignAgentToDepartment(agentId, departmentId);
            return ResponseEntity.ok(Map.of("message", "Agent assigned to department"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest(e.getMessage());
        }
    }

    /**
     * DELETE /api/users/{id}/department/{deptId}
     */
    public ResponseEntity deleteAgentFromDepartment(String agentId, String departmentId) {
        try {
            manageAssigneeUseCase.removeAgentFromDepartment(agentId, departmentId);
            return ResponseEntity.ok(Map.of("message", "Agent removed from department"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest(e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String requireField(Map<String, String> body, String field) {
        if (body == null || !body.containsKey(field) || body.get(field).isBlank()) {
            throw new IllegalArgumentException("Missing required field: " + field);
        }
        return body.get(field).trim();
    }

    private AgentStatus parseAgentStatus(String value) {
        try {
            return AgentStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid agent status: '" + value
                    + "'. Valid values: AVAILABLE, BUSY, UNAVAILABLE");
        }
    }

    // -------------------------------------------------------------------------
    // Minimal response wrapper (no framework)
    // -------------------------------------------------------------------------

    public record ResponseEntity(int statusCode, Object body) {
        public static ResponseEntity ok(Object body) {
            return new ResponseEntity(200, body);
        }
        public static ResponseEntity badRequest(String message) {
            return new ResponseEntity(400, Map.of("error", message));
        }
        public static ResponseEntity notFound(String message) {
            return new ResponseEntity(404, Map.of("error", message));
        }
        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }
    }
}
