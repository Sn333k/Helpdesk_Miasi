package com.miasi.users.domain;

import com.miasi.users.domain.model.*;
import com.miasi.users.domain.services.StaffAssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StaffAssignmentServiceTest {

    private StaffAssignmentService service;
    private SupportTeam networkTeam;
    private Assignee agentA;
    private Assignee agentB;
    private Assignee agentC;

    @BeforeEach
    void setUp() {
        service = new StaffAssignmentService();

        networkTeam = SupportTeamFactory.create("Network Team", Set.of("networking"));

        agentA = AssigneeFactory.create("agent-a@example.com");
        agentB = AssigneeFactory.create("agent-b@example.com");
        agentC = AssigneeFactory.create("agent-c@example.com");

        networkTeam.assignAgent(agentA.getUserId());
        networkTeam.assignAgent(agentB.getUserId());
        // agentC is NOT in the network team
    }

    @Test
    void findOptimalAgent_returnsAgentWithLowestLoad() {
        // agentB has higher load
        agentB.incrementLoad();
        agentB.incrementLoad();

        Optional<Assignee> result = service.findOptimalAgent(
                "networking",
                List.of(networkTeam),
                List.of(agentA, agentB));

        assertTrue(result.isPresent());
        assertEquals(agentA.getUserId(), result.get().getUserId());
    }

    @Test
    void findOptimalAgent_excludesUnavailableAgents() {
        agentA.changeAvailability(AgentStatus.UNAVAILABLE);
        agentA.pullDomainEvents();

        Optional<Assignee> result = service.findOptimalAgent(
                "networking",
                List.of(networkTeam),
                List.of(agentA, agentB));

        assertTrue(result.isPresent());
        assertEquals(agentB.getUserId(), result.get().getUserId());
    }

    @Test
    void findOptimalAgent_noMatchingTeam_returnsEmpty() {
        Optional<Assignee> result = service.findOptimalAgent(
                "billing",
                List.of(networkTeam),
                List.of(agentA, agentB));

        assertTrue(result.isEmpty());
    }

    @Test
    void findOptimalAgent_agentNotInEligibleTeam_excluded() {
        // agentC is available but not in any team that handles "networking"
        Optional<Assignee> result = service.findOptimalAgent(
                "networking",
                List.of(networkTeam),
                List.of(agentC));  // only agentC as candidate

        assertTrue(result.isEmpty());
    }

    @Test
    void findOptimalAgent_allAgentsUnavailable_returnsEmpty() {
        agentA.changeAvailability(AgentStatus.UNAVAILABLE);
        agentB.changeAvailability(AgentStatus.UNAVAILABLE);

        Optional<Assignee> result = service.findOptimalAgent(
                "networking",
                List.of(networkTeam),
                List.of(agentA, agentB));

        assertTrue(result.isEmpty());
    }

    @Test
    void findOptimalAgent_blankCategory_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.findOptimalAgent("  ", List.of(networkTeam), List.of(agentA)));
    }

    @Test
    void findEligibleAgentIds_returnsCorrectIds() {
        List<UserID> eligible = service.findEligibleAgentIds("networking", List.of(networkTeam));
        assertEquals(2, eligible.size());
        assertTrue(eligible.contains(agentA.getUserId()));
        assertTrue(eligible.contains(agentB.getUserId()));
        assertFalse(eligible.contains(agentC.getUserId()));
    }
}
