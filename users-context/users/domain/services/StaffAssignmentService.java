package com.miasi.users.domain.services;

import com.miasi.users.domain.model.Assignee;
import com.miasi.users.domain.model.SupportTeam;
import com.miasi.users.domain.model.UserID;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Domain Service: StaffAssignmentService
 *
 * Determines the optimal agent for a ticket of a given category.
 * Logic:
 *   1. Filter SupportTeams that support the requested category.
 *   2. From those teams, collect all assigned agent IDs.
 *   3. From the available agents (provided as candidates), pick the one
 *      with the lowest currentLoad.
 *
 * This service is pure — it takes all inputs as parameters and has no
 * dependencies on repositories (those are handled in the application layer).
 */
public class StaffAssignmentService {

    /**
     * Finds the optimal available agent for the given category.
     *
     * @param category   the ticket category to match
     * @param teams      all SupportTeams in the system
     * @param candidates all currently available Assignees
     * @return the best matching Assignee, or empty if none found
     */
    public Optional<Assignee> findOptimalAgent(String category,
                                               List<SupportTeam> teams,
                                               List<Assignee> candidates) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category must not be blank");
        }
        if (teams == null || candidates == null) {
            return Optional.empty();
        }

        // Collect agent IDs from teams supporting this category
        var eligibleAgentIds = teams.stream()
                .filter(team -> team.supportsCategory(category))
                .flatMap(team -> team.getAssignedAgents().stream())
                .collect(java.util.stream.Collectors.toSet());

        if (eligibleAgentIds.isEmpty()) {
            return Optional.empty();
        }

        // From candidates, find those in eligible teams with lowest load
        return candidates.stream()
                .filter(Assignee::isAvailable)
                .filter(a -> eligibleAgentIds.contains(a.getUserId()))
                .min(Comparator.comparingInt(Assignee::getCurrentLoad));
    }

    /**
     * Returns all eligible agent IDs for a given category (used by OHS adapter).
     */
    public List<UserID> findEligibleAgentIds(String category, List<SupportTeam> teams) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category must not be blank");
        }
        return teams.stream()
                .filter(team -> team.supportsCategory(category))
                .flatMap(team -> team.getAssignedAgents().stream())
                .distinct()
                .toList();
    }
}
