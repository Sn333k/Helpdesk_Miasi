package com.miasi.users.application.ports.outbound;

import com.miasi.users.domain.model.Assignee;
import com.miasi.users.domain.model.Requester;
import com.miasi.users.domain.model.UserID;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port: persistence contract for Users (Assignee + Requester).
 */
public interface IUserRepository {

    // --- Assignee ---

    void saveAssignee(Assignee assignee);

    Optional<Assignee> findAssigneeById(UserID id);

    List<Assignee> findAllAssignees();

    List<Assignee> findAvailableAssignees();

    // --- Requester ---

    void saveRequester(Requester requester);

    Optional<Requester> findRequesterById(UserID id);

    List<Requester> findAllRequesters();

    boolean existsByEmail(String email);
}
