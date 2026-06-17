package com.miasi.users.domain.model;

import com.miasi.users.domain.events.AgentAssignedToTeam;
import com.miasi.users.domain.events.DomainEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * SupportTeam aggregate root. Represents a team of agents that handle a specific area of interest.
 */
public class SupportTeam {

  private final SupportTeamID id;
  private final String name;
  private final String areaOfInterest;
  private final Set<UserID> assignedAgents;
  private final List<DomainEvent> domainEvents;

  public SupportTeam(String name, String areaOfInterest) {
    if (name == null || name.isBlank())
      throw new IllegalArgumentException("SupportTeam name must not be blank");
    if (areaOfInterest == null || areaOfInterest.isBlank())
      throw new IllegalArgumentException("Area of interest must not be blank");

    this.id = new SupportTeamID(UUID.randomUUID().toString());
    this.name = name;
    this.areaOfInterest = areaOfInterest;
    this.assignedAgents = new HashSet<>();
    this.domainEvents = new ArrayList<>();
  }

  /** Package-private reconstitution constructor — for repository use only. */
  SupportTeam(SupportTeamID id, String name, String areaOfInterest, Set<UserID> assignedAgents) {
    this.id = id;
    this.name = name;
    this.areaOfInterest = areaOfInterest;
    this.assignedAgents = new HashSet<>(assignedAgents);
    this.domainEvents = new ArrayList<>();
  }

  public static SupportTeam reconstitute(
      SupportTeamID id, String name, String areaOfInterest, Set<UserID> assignedAgents) {
    return new SupportTeam(id, name, areaOfInterest, assignedAgents);
  }

  /** Assigns an agent to this team. Invariant: an agent cannot be added twice. */
  public void assignAgent(UserID agentId) {
    if (agentId == null) throw new IllegalArgumentException("AgentId must not be null");
    if (assignedAgents.contains(agentId))
      throw new IllegalStateException(
          "Agent " + agentId.id() + " is already assigned to team " + name);

    assignedAgents.add(agentId);
    domainEvents.add(new AgentAssignedToTeam(agentId, id, Instant.now()));
  }

  /** Removes an agent from this team. Invariant: agent must currently be a member. */
  public void removeAgent(UserID agentId) {
    if (agentId == null) throw new IllegalArgumentException("AgentId must not be null");
    if (!assignedAgents.contains(agentId))
      throw new IllegalStateException("Agent " + agentId.id() + " is not a member of team " + name);

    assignedAgents.remove(agentId);
  }

  public boolean supportsAreaOfInterest(String area) {
    if (area == null) return false;
    return this.areaOfInterest.equalsIgnoreCase(area.trim());
  }

  public boolean hasMember(UserID agentId) {
    return assignedAgents.contains(agentId);
  }

  public List<DomainEvent> popEvents() {
    List<DomainEvent> events = new ArrayList<>(domainEvents);
    domainEvents.clear();
    return Collections.unmodifiableList(events);
  }

  public SupportTeamID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getAreaOfInterest() {
    return areaOfInterest;
  }

  public Set<UserID> getAssignedAgents() {
    return Collections.unmodifiableSet(assignedAgents);
  }
}
