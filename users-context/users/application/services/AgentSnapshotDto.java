package com.miasi.users.application.services;

/**
 * Read-model DTO returned by the OHS adapter to the Ticket bounded context.
 * Contains only the information needed for ticket assignment decisions.
 */
public record AgentSnapshotDto(
        String agentId,
        String email,
        int currentLoad,
        boolean available
) {
    public AgentSnapshotDto {
        if (agentId == null || agentId.isBlank()) {
            throw new IllegalArgumentException("agentId must not be blank");
        }
    }
}
