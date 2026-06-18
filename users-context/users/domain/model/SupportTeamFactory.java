package com.miasi.users.domain.model;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Factory for creating SupportTeam aggregates.
 */
public final class SupportTeamFactory {

    private SupportTeamFactory() {}

    public static SupportTeam create(String name, Set<String> areaNames) {
        SupportTeamID id = SupportTeamID.generate();
        Set<AreaOfInterest> areas = areaNames.stream()
                .map(AreaOfInterest::of)
                .collect(Collectors.toSet());
        return new SupportTeam(id, name, areas);
    }

    public static SupportTeam reconstitute(String teamId, String name,
                                            Set<String> areaNames,
                                            Set<String> agentIds) {
        Set<AreaOfInterest> areas = areaNames.stream()
                .map(AreaOfInterest::of)
                .collect(Collectors.toSet());
        SupportTeam team = new SupportTeam(SupportTeamID.of(teamId), name, areas);
        agentIds.stream()
                .map(UserID::of)
                .forEach(team.assignedAgents::add);
        return team;
    }
}
