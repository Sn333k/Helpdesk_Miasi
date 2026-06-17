package com.miasi.users.infrastructure.adapters.outbound;

import com.miasi.users.application.ports.outbound.IDepartmentRepository;
import com.miasi.users.domain.model.SupportTeam;
import com.miasi.users.domain.model.SupportTeamID;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of IDepartmentRepository.
 * Named JpaDepartmentRepository to match the project class diagram.
 */
public class JpaDepartmentRepository implements IDepartmentRepository {

    private final Map<String, SupportTeam> store = new ConcurrentHashMap<>();

    @Override
    public void save(SupportTeam supportTeam) {
        Objects.requireNonNull(supportTeam, "supportTeam must not be null");
        store.put(supportTeam.getSupportTeamID().getId(), supportTeam);
    }

    @Override
    public Optional<SupportTeam> findById(SupportTeamID id) {
        Objects.requireNonNull(id, "id must not be null");
        return Optional.ofNullable(store.get(id.getId()));
    }

    @Override
    public List<SupportTeam> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public List<SupportTeam> findByCategory(String category) {
        if (category == null || category.isBlank()) return List.of();
        return store.values().stream()
                .filter(team -> team.supportsCategory(category))
                .toList();
    }
}
