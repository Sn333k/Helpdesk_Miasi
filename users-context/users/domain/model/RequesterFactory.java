package com.miasi.users.domain.model;

/**
 * Factory for creating Requester aggregates.
 */
public final class RequesterFactory {

    private RequesterFactory() {}

    public static Requester create(String email) {
        UserID id = UserID.generate();
        EmailAddress emailAddress = EmailAddress.of(email);
        return new Requester(id, emailAddress);
    }

    public static Requester reconstitute(String userId, String email,
                                         AccountStatus accountStatus,
                                         int currentOpen) {
        Requester requester = new Requester(UserID.of(userId), EmailAddress.of(email));
        if (accountStatus == AccountStatus.SUSPENDED) {
            requester.accountStatus = AccountStatus.SUSPENDED;
        }
        requester.currentOpen = currentOpen;
        return requester;
    }
}
