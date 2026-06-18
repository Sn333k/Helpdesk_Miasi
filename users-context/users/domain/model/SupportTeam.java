package com.miasi.users.domain.model;

import com.miasi.users.domain.events.AgentAssignedToDepartment;
import com.miasi.users.domain.events.DomainEvent;

import java.util.*;

/**
 * Aggregate Root: SupportTeam
 *
 * Represents a team of agents handling tickets within specific areas of interest.
 *
 * Invariants:
 *   - Name must not be blank.
 *   - Must have at least one AreaOfInterest.
 *   - The same agent cannot be assigned twice.
 *
 * State transitions: CREATED -> MODIFIED (self-loop on every change)
 */
public class SupportTeam {

    public enum State { CREATED, MODIFIED }

    private final SupportTeamID supportTeamID;
    private String name;
    private final Set<AreaOfInterest> areasOfInterest;
    private final Set<UserID> assignedAgents;
    private State state;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // Package-private: use SupportTeamFactory
    SupportTeam(SupportTeamID supportTeamID, String name, Set<AreaOfInterest> areasOfInterest) {
        Objects.requireNonNull(supportTeamID, "supportTeamID must not be null");
        validateName(name);
        validateAreasOfInterest(areasOfInterest);

        this.supportTeamID = supportTeamID;
        this.name = name;
        this.areasOfInterest = new HashSet<>(areasOfInterest);
        this.assignedAgents = new HashSet<>();
        this.state = State.CREATED;
    }

    // -------------------------------------------------------------------------
    // Commands
    // -------------------------------------------------------------------------

    /**
     * Assigns an agent to this team.
     *
     * @throws IllegalArgumentException if agent is already assigned
     */
    public void assignAgent(UserID agentId) {
        Objects.requireNonNull(agentId, "agentId must not be null");
        if (assignedAgents.contains(agentId)) {
            throw new IllegalArgumentException(
                    "Agent " + agentId + " is already assigned to team " + supportTeamID);
        }
        assignedAgents.add(agentId);
        state = State.MODIFIED;
        domainEvents.add(new AgentAssignedToDepartment(agentId, supportTeamID));
    }

    /**
     * Removes an agent from this team.
     * Called as a corrective policy when a user deletion event is detected.
     */
    public void removeAgent(UserID agentId) {
        Objects.requireNonNull(agentId, "agentId must not be null");
        assignedAgents.remove(agentId);
        state = State.MODIFIED;
    }

    /**
     * Adds a new area of interest to the team.
     * Raises SupportTeamCompetencesExtended (modelled as AgentAssignedToDepartment-equivalent).
     */
    public void addAreaOfInterest(AreaOfInterest area) {
        Objects.requireNonNull(area, "area must not be null");
        areasOfInterest.add(area);
        state = State.MODIFIED;
    }

    /**
     * Updates the team name.
     */
    public void rename(String newName) {
        validateName(newName);
        this.name = newName;
        state = State.MODIFIED;
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /**
     * Returns true if this team handles the given category.
     */
    public boolean supportsCategory(String category) {
        return areasOfInterest.stream().anyMatch(a -> a.matches(category));
    }

    /**
     * Returns true if this team has any available agents.
     */
    public boolean hasAgent(UserID agentId) {
        return assignedAgents.contains(agentId);
    }

    public SupportTeamID getSupportTeamID() { return supportTeamID; }
    public String getName() { return name; }
    public Set<AreaOfInterest> getAreasOfInterest() { return Collections.unmodifiableSet(areasOfInterest); }
    public Set<UserID> getAssignedAgents() { return Collections.unmodifiableSet(assignedAgents); }
    public State getState() { return state; }

    // -------------------------------------------------------------------------
    // Event handling
    // -------------------------------------------------------------------------

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = Collections.unmodifiableList(new ArrayList<>(domainEvents));
        domainEvents.clear();
        return events;
    }

    // -------------------------------------------------------------------------
    // Validation helpers
    // -------------------------------------------------------------------------

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("SupportTeam name must not be blank");
        }
    }

    private static void validateAreasOfInterest(Set<AreaOfInterest> areas) {
        if (areas == null || areas.isEmpty()) {
            throw new IllegalArgumentException(
                    "SupportTeam must have at least one AreaOfInterest");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupportTeam)) return false;
        SupportTeam that = (SupportTeam) o;
        return Objects.equals(supportTeamID, that.supportTeamID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(supportTeamID);
    }

    @Override
    public String toString() {
        return "SupportTeam{id=" + supportTeamID
                + ", name='" + name + "'"
                + ", areas=" + areasOfInterest
                + ", agents=" + assignedAgents.size()
                + ", state=" + state + "}";
    }
}
