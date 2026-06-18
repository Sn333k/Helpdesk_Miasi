package com.miasi.users.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class SupportTeamID {

    private final String id;

    private SupportTeamID(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("SupportTeamID cannot be null or blank");
        }
        this.id = id;
    }

    public static SupportTeamID of(String id) {
        return new SupportTeamID(id);
    }

    public static SupportTeamID generate() {
        return new SupportTeamID(UUID.randomUUID().toString());
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupportTeamID)) return false;
        SupportTeamID that = (SupportTeamID) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SupportTeamID{" + id + "}";
    }
}
