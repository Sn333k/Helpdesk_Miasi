package com.miasi.users.application.services;

import com.miasi.users.application.ports.inbound.ChangeAgentAvailabilityUseCase;
import com.miasi.users.application.ports.inbound.ManageAssigneeUseCase;
import com.miasi.users.application.ports.outbound.IDepartmentRepository;
import com.miasi.users.application.ports.outbound.IUserRepository;
import com.miasi.users.domain.events.DomainEventPublisher;
import com.miasi.users.domain.model.*;
import com.miasi.users.domain.services.StaffAssignmentService;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Application Service: UserApplicationService
 *
 * Orchestrates use-cases for the Users bounded context.
 * Implements both inbound ports, coordinates between domain objects,
 * repositories and the domain event publisher.
 */
public class UserApplicationService
        implements ChangeAgentAvailabilityUseCase, ManageAssigneeUseCase {

    private final IUserRepository userRepository;
    private final IDepartmentRepository departmentRepository;
    private final StaffAssignmentService staffAssignmentService;
    private final DomainEventPublisher eventPublisher;

    public UserApplicationService(IUserRepository userRepository,
                                   IDepartmentRepository departmentRepository,
                                   StaffAssignmentService staffAssignmentService,
                                   DomainEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.staffAssignmentService = staffAssignmentService;
        this.eventPublisher = eventPublisher;
    }

    // -------------------------------------------------------------------------
    // ChangeAgentAvailabilityUseCase
    // -------------------------------------------------------------------------

    @Override
    public void changeAvailability(String agentId, AgentStatus newStatus) {
        if (agentId == null || agentId.isBlank()) {
            throw new IllegalArgumentException("agentId must not be blank");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("newStatus must not be null");
        }

        Assignee assignee = userRepository.findAssigneeById(UserID.of(agentId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Assignee not found: " + agentId));

        assignee.changeAvailability(newStatus);
        userRepository.saveAssignee(assignee);
        eventPublisher.publishAll(assignee.pullDomainEvents());
    }

    // -------------------------------------------------------------------------
    // ManageAssigneeUseCase
    // -------------------------------------------------------------------------

    @Override
    public String createAssignee(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("User with email already exists: " + email);
        }

        Assignee assignee = AssigneeFactory.create(email);
        userRepository.saveAssignee(assignee);
        eventPublisher.publishAll(assignee.pullDomainEvents());
        return assignee.getUserId().getId();
    }

    @Override
    public String createRequester(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("User with email already exists: " + email);
        }

        Requester requester = RequesterFactory.create(email);
        userRepository.saveRequester(requester);
        eventPublisher.publishAll(requester.pullDomainEvents());
        return requester.getUserId().getId();
    }

    @Override
    public void assignAgentToDepartment(String agentId, String departmentId) {
        if (agentId == null || agentId.isBlank()) {
            throw new IllegalArgumentException("agentId must not be blank");
        }
        if (departmentId == null || departmentId.isBlank()) {
            throw new IllegalArgumentException("departmentId must not be blank");
        }

        // Verify agent exists
        userRepository.findAssigneeById(UserID.of(agentId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Assignee not found: " + agentId));

        SupportTeam team = departmentRepository.findById(SupportTeamID.of(departmentId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "SupportTeam not found: " + departmentId));

        team.assignAgent(UserID.of(agentId));
        departmentRepository.save(team);
        eventPublisher.publishAll(team.pullDomainEvents());
    }

    @Override
    public void removeAgentFromDepartment(String agentId, String departmentId) {
        if (agentId == null || agentId.isBlank()) {
            throw new IllegalArgumentException("agentId must not be blank");
        }
        if (departmentId == null || departmentId.isBlank()) {
            throw new IllegalArgumentException("departmentId must not be blank");
        }

        SupportTeam team = departmentRepository.findById(SupportTeamID.of(departmentId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "SupportTeam not found: " + departmentId));

        team.removeAgent(UserID.of(agentId));
        departmentRepository.save(team);
        eventPublisher.publishAll(team.pullDomainEvents());
    }

    @Override
    public void suspendAgent(String agentId) {
        Assignee assignee = userRepository.findAssigneeById(UserID.of(agentId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Assignee not found: " + agentId));

        assignee.suspend();
        userRepository.saveAssignee(assignee);
        eventPublisher.publishAll(assignee.pullDomainEvents());
    }

    @Override
    public void activateAgent(String agentId) {
        Assignee assignee = userRepository.findAssigneeById(UserID.of(agentId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Assignee not found: " + agentId));

        assignee.activate();
        userRepository.saveAssignee(assignee);
        eventPublisher.publishAll(assignee.pullDomainEvents());
    }

    @Override
    public String createSupportTeam(String name, String areas) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Team name must not be blank");
        }
        if (areas == null || areas.isBlank()) {
            throw new IllegalArgumentException("Areas of interest must not be blank");
        }

        Set<String> areaSet = Arrays.stream(areas.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        if (areaSet.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one valid area of interest is required");
        }

        SupportTeam team = SupportTeamFactory.create(name, areaSet);
        departmentRepository.save(team);
        eventPublisher.publishAll(team.pullDomainEvents());
        return team.getSupportTeamID().getId();
    }

    // -------------------------------------------------------------------------
    // Query: used by OHS adapter
    // -------------------------------------------------------------------------

    /**
     * Returns agent snapshots for agents eligible for a given category.
     * Called by HelpdeskInternalClientAdapter to serve the Ticket context.
     */
    public List<AgentSnapshotDto> getAvailableAgentsForCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category must not be blank");
        }

        List<SupportTeam> teams = departmentRepository.findAll();
        List<Assignee> allAssignees = userRepository.findAllAssignees();

        List<UserID> eligibleIds = staffAssignmentService
                .findEligibleAgentIds(category, teams);

        return allAssignees.stream()
                .filter(a -> eligibleIds.contains(a.getUserId()))
                .filter(a -> a.getAccountStatus() == AccountStatus.ACTIVE)
                .map(a -> new AgentSnapshotDto(
                        a.getUserId().getId(),
                        a.getEmail().getEmail(),
                        a.getCurrentLoad(),
                        a.isAvailable()))
                .toList();
    }
}
