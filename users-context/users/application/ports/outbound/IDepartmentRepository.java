package com.miasi.users.application.ports.outbound;

import com.miasi.users.domain.model.SupportTeam;
import com.miasi.users.domain.model.SupportTeamID;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port: persistence contract for SupportTeam (Department).
 */
public interface IDepartmentRepository {

    void save(SupportTeam supportTeam);

    Optional<SupportTeam> findById(SupportTeamID id);

    List<SupportTeam> findAll();

    /**
     * Returns all teams that support the given category.
     */
    List<SupportTeam> findByCategory(String category);
}
