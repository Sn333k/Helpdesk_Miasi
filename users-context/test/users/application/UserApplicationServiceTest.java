package com.miasi.users.application;

import com.miasi.users.application.services.AgentSnapshotDto;
import com.miasi.users.application.services.UserApplicationService;
import com.miasi.users.domain.events.AgentAvailabilityChanged;
import com.miasi.users.domain.events.DomainEvent;
import com.miasi.users.domain.events.DomainEventPublisher;
import com.miasi.users.domain.model.AgentStatus;
import com.miasi.users.domain.services.StaffAssignmentService;
import com.miasi.users.infrastructure.adapters.outbound.JpaDepartmentRepository;
import com.miasi.users.infrastructure.adapters.outbound.JpaUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserApplicationServiceTest {

    private UserApplicationService service;
    private JpaUserRepository userRepository;
    private JpaDepartmentRepository departmentRepository;
    private DomainEventPublisher publisher;
    private List<DomainEvent> publishedEvents;

    @BeforeEach
    void setUp() {
        userRepository = new JpaUserRepository();
        departmentRepository = new JpaDepartmentRepository();
        publisher = DomainEventPublisher.instance();
        publisher.reset();

        publishedEvents = new ArrayList<>();
        publisher.subscribe(DomainEvent.class, publishedEvents::add);

        service = new UserApplicationService(
                userRepository,
                departmentRepository,
                new StaffAssignmentService(),
                publisher);
    }

    @Test
    void createAssignee_storesAndReturnsId() {
        String id = service.createAssignee("agent@example.com");
        assertNotNull(id);
        assertFalse(id.isBlank());
        assertTrue(userRepository.findAssigneeById(
                com.miasi.users.domain.model.UserID.of(id)).isPresent());
    }

    @Test
    void createAssignee_duplicateEmail_throws() {
        service.createAssignee("agent@example.com");
        assertThrows(IllegalStateException.class,
                () -> service.createAssignee("agent@example.com"));
    }

    @Test
    void createRequester_storesAndReturnsId() {
        String id = service.createRequester("user@example.com");
        assertNotNull(id);
        assertTrue(userRepository.findRequesterById(
                com.miasi.users.domain.model.UserID.of(id)).isPresent());
    }

    @Test
    void createSupportTeam_storesTeam() {
        String teamId = service.createSupportTeam("Network Team", "networking, hardware");
        assertNotNull(teamId);
        assertTrue(departmentRepository.findById(
                com.miasi.users.domain.model.SupportTeamID.of(teamId)).isPresent());
    }

    @Test
    void createSupportTeam_blankAreas_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createSupportTeam("Team", "  "));
    }

    @Test
    void assignAgentToDepartment_linksAgentToTeam() {
        String agentId = service.createAssignee("agent@example.com");
        String teamId = service.createSupportTeam("Net Team", "networking");

        service.assignAgentToDepartment(agentId, teamId);

        var team = departmentRepository.findById(
                com.miasi.users.domain.model.SupportTeamID.of(teamId)).orElseThrow();
        assertTrue(team.hasAgent(com.miasi.users.domain.model.UserID.of(agentId)));
    }

    @Test
    void changeAvailability_updatesAgentAndPublishesEvent() {
        String agentId = service.createAssignee("agent@example.com");
        publishedEvents.clear();

        service.changeAvailability(agentId, AgentStatus.UNAVAILABLE);

        var agent = userRepository.findAssigneeById(
                com.miasi.users.domain.model.UserID.of(agentId)).orElseThrow();
        assertEquals(AgentStatus.UNAVAILABLE, agent.getAgentStatus());

        assertEquals(1, publishedEvents.size());
        assertInstanceOf(AgentAvailabilityChanged.class, publishedEvents.get(0));
    }

    @Test
    void suspendAgent_makesUnavailableAndPublishesEvent() {
        String agentId = service.createAssignee("agent@example.com");
        publishedEvents.clear();

        service.suspendAgent(agentId);

        var agent = userRepository.findAssigneeById(
                com.miasi.users.domain.model.UserID.of(agentId)).orElseThrow();
        assertFalse(agent.isAvailable());
        assertTrue(publishedEvents.stream()
                .anyMatch(e -> e instanceof AgentAvailabilityChanged));
    }

    @Test
    void getAvailableAgentsForCategory_returnsMatchingAgents() {
        String agentId = service.createAssignee("net-agent@example.com");
        String teamId = service.createSupportTeam("Net Team", "networking");
        service.assignAgentToDepartment(agentId, teamId);

        List<AgentSnapshotDto> result =
                service.getAvailableAgentsForCategory("networking");

        assertEquals(1, result.size());
        assertEquals(agentId, result.get(0).agentId());
        assertTrue(result.get(0).available());
    }

    @Test
    void getAvailableAgentsForCategory_excludesSuspendedAgents() {
        String agentId = service.createAssignee("net-agent@example.com");
        String teamId = service.createSupportTeam("Net Team", "networking");
        service.assignAgentToDepartment(agentId, teamId);
        service.suspendAgent(agentId);

        List<AgentSnapshotDto> result =
                service.getAvailableAgentsForCategory("networking");

        assertTrue(result.isEmpty());
    }

    @Test
    void changeAvailability_unknownAgent_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.changeAvailability("non-existent-id", AgentStatus.BUSY));
    }
}
