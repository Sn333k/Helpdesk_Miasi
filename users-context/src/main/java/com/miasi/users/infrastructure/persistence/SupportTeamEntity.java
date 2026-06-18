package com.miasi.users.infrastructure.persistence;

import com.miasi.users.domain.model.SupportTeam;
import com.miasi.users.domain.model.SupportTeamID;
import com.miasi.users.domain.model.UserID;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "support_teams")
public class SupportTeamEntity {

  @Id private String id;
  private String name;
  private String areaOfInterest;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "team_agents", joinColumns = @JoinColumn(name = "team_id"))
  @Column(name = "agent_id")
  private Set<String> assignedAgentIds = new HashSet<>();

  public SupportTeamEntity() {}

  public SupportTeamEntity(SupportTeam team) {
    this.id = team.getId().id();
    this.name = team.getName();
    this.areaOfInterest = team.getAreaOfInterest();
    this.assignedAgentIds =
        team.getAssignedAgents().stream().map(UserID::id).collect(Collectors.toSet());
  }

  public SupportTeam toDomain() {
    Set<UserID> agents = assignedAgentIds.stream().map(UserID::new).collect(Collectors.toSet());
    return SupportTeam.reconstitute(new SupportTeamID(id), name, areaOfInterest, agents);
  }
}
