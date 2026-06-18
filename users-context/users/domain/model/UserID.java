package com.miasi.users.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class UserID {

    private final String id;

    private UserID(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("UserID cannot be null or blank");
        }
        this.id = id;
    }

    public static UserID of(String id) {
        return new UserID(id);
    }

    public static UserID generate() {
        return new UserID(UUID.randomUUID().toString());
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserID)) return false;
        UserID userID = (UserID) o;
        return Objects.equals(id, userID.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "UserID{" + id + "}";
    }
}
