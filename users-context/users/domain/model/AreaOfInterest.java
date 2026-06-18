package com.miasi.users.domain.model;

import java.util.Objects;

/**
 * Represents a category/specialization area that a SupportTeam handles.
 * Matches the "kategoria" concept from the domain — each SupportTeam
 * covers one or more areas and receives tickets of matching categories.
 */
public final class AreaOfInterest {

    private final String name;

    private AreaOfInterest(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("AreaOfInterest name cannot be null or blank");
        }
        this.name = name.trim().toLowerCase();
    }

    public static AreaOfInterest of(String name) {
        return new AreaOfInterest(name);
    }

    public String getName() {
        return name;
    }

    public boolean matches(String category) {
        if (category == null) return false;
        return this.name.equals(category.trim().toLowerCase());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AreaOfInterest)) return false;
        AreaOfInterest that = (AreaOfInterest) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "AreaOfInterest{" + name + "}";
    }
}
