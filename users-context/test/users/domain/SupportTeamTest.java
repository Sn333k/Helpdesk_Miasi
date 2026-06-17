package com.miasi.users.domain;

import com.miasi.users.domain.events.AgentAssignedToDepartment;
import com.miasi.users.domain.events.DomainEvent;
import com.miasi.users.domain.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SupportTeamTest {

    private SupportTeam createTeam() {
        return SupportTeamFactory.create("Network Team", Set.of("networking", "hardware"));
    }

    @Test
    void newTeam_hasCreatedState() {
        SupportTeam team = createTeam();
        assertEquals(SupportTeam.State.CREATED, team.getState());
    }

    @Test
    void teamCreation_withBlankName_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> SupportTeamFactory.create("  ", Set.of("networking")));
    }

    @Test
    void teamCreation_withNoAreas_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> SupportTeamFactory.create("Team", Set.of()));
    }

    @Test
    void assignAgent_publishesEvent_andTransitionsToModified() {
        SupportTeam team = createTeam();
        UserID agentId = UserID.generate();

        team.assignAgent(agentId);

        assertEquals(SupportTeam.State.MODIFIED, team.getState());
        assertTrue(team.hasAgent(agentId));

        List<DomainEvent> events = team.pullDomainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(AgentAssignedToDepartment.class, events.get(0));
    }

    @Test
    void assignSameAgent_twice_throws() {
        SupportTeam team = createTeam();
        UserID agentId = UserID.generate();
        team.assignAgent(agentId);
        team.pullDomainEvents();

        assertThrows(IllegalArgumentException.class, () -> team.assignAgent(agentId));
    }

    @Test
    void removeAgent_removesFromSet() {
        SupportTeam team = createTeam();
        UserID agentId = UserID.generate();
        team.assignAgent(agentId);
        team.pullDomainEvents();

        team.removeAgent(agentId);
        assertFalse(team.hasAgent(agentId));
    }

    @Test
    void supportsCategory_matchesCorrectly() {
        SupportTeam team = createTeam();
        assertTrue(team.supportsCategory("networking"));
        assertTrue(team.supportsCategory("NETWORKING")); // case-insensitive
        assertFalse(team.supportsCategory("billing"));
    }

    @Test
    void addAreaOfInterest_expandsCompetences() {
        SupportTeam team = createTeam();
        team.addAreaOfInterest(AreaOfInterest.of("billing"));
        assertTrue(team.supportsCategory("billing"));
        assertEquals(SupportTeam.State.MODIFIED, team.getState());
    }

    @Test
    void pullDomainEvents_clearsEventList() {
        SupportTeam team = createTeam();
        team.assignAgent(UserID.generate());
        assertFalse(team.pullDomainEvents().isEmpty());
        assertTrue(team.pullDomainEvents().isEmpty());
    }
}
