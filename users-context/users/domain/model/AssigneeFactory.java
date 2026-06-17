package com.miasi.users.domain.model;

import java.util.Set;

/**
 * Factory for creating Assignee aggregates.
 * Centralises construction logic and ensures all invariants are met at creation time.
 */
public final class AssigneeFactory {

    private AssigneeFactory() {}

    public static Assignee create(String email) {
        UserID id = UserID.generate();
        EmailAddress emailAddress = EmailAddress.of(email);
        return new Assignee(id, emailAddress);
    }

    public static Assignee reconstitute(String userId, String email,
                                        AccountStatus accountStatus,
                                        AgentStatus agentStatus,
                                        int currentLoad) {
        Assignee assignee = new Assignee(UserID.of(userId), EmailAddress.of(email));
        // Restore state without triggering events (used when loading from persistence)
        if (accountStatus == AccountStatus.SUSPENDED) {
            assignee.accountStatus = AccountStatus.SUSPENDED;
        }
        assignee.agentStatus = agentStatus;
        assignee.currentLoad = currentLoad;
        return assignee;
    }
}
