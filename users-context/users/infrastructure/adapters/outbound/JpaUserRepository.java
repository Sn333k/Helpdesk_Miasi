package com.miasi.users.infrastructure.adapters.outbound;

import com.miasi.users.application.ports.outbound.IUserRepository;
import com.miasi.users.domain.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of IUserRepository.
 * Replace with a JPA/JDBC implementation for production persistence.
 *
 * Named JpaUserRepository to match the project structure shown in the class diagram;
 * the actual persistence strategy is swappable behind the port interface.
 */
public class JpaUserRepository implements IUserRepository {

    private final Map<String, Assignee> assignees = new ConcurrentHashMap<>();
    private final Map<String, Requester> requesters = new ConcurrentHashMap<>();

    // --- Assignee ---

    @Override
    public void saveAssignee(Assignee assignee) {
        Objects.requireNonNull(assignee, "assignee must not be null");
        assignees.put(assignee.getUserId().getId(), assignee);
    }

    @Override
    public Optional<Assignee> findAssigneeById(UserID id) {
        Objects.requireNonNull(id, "id must not be null");
        return Optional.ofNullable(assignees.get(id.getId()));
    }

    @Override
    public List<Assignee> findAllAssignees() {
        return List.copyOf(assignees.values());
    }

    @Override
    public List<Assignee> findAvailableAssignees() {
        return assignees.values().stream()
                .filter(Assignee::isAvailable)
                .toList();
    }

    // --- Requester ---

    @Override
    public void saveRequester(Requester requester) {
        Objects.requireNonNull(requester, "requester must not be null");
        requesters.put(requester.getUserId().getId(), requester);
    }

    @Override
    public Optional<Requester> findRequesterById(UserID id) {
        Objects.requireNonNull(id, "id must not be null");
        return Optional.ofNullable(requesters.get(id.getId()));
    }

    @Override
    public List<Requester> findAllRequesters() {
        return List.copyOf(requesters.values());
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null) return false;
        String normalised = email.trim().toLowerCase();
        boolean inAssignees = assignees.values().stream()
                .anyMatch(a -> a.getEmail().getEmail().equals(normalised));
        if (inAssignees) return true;
        return requesters.values().stream()
                .anyMatch(r -> r.getEmail().getEmail().equals(normalised));
    }
}
