package com.miasi.users.infrastructure.rest;

import com.miasi.users.application.ports.inbound.AssignAgentToTeamUseCase;
import com.miasi.users.application.ports.inbound.ChangeAgentAvailabilityUseCase;
import com.miasi.users.application.ports.inbound.ChangeEmailUseCase;
import com.miasi.users.application.ports.inbound.CreateAssigneeUseCase;
import com.miasi.users.application.ports.inbound.CreateRequesterUseCase;
import com.miasi.users.application.ports.inbound.CreateSupportTeamUseCase;
import com.miasi.users.application.ports.inbound.GetAvailableAgentsUseCase;
import com.miasi.users.application.ports.inbound.RemoveAgentFromTeamUseCase;
import com.miasi.users.domain.model.AgentSnapshot;
import com.miasi.users.domain.model.AgentStatus;
import com.miasi.users.domain.model.EmailAddress;
import com.miasi.users.domain.model.SupportTeamID;
import com.miasi.users.domain.model.UserID;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.http.Context;
import java.util.List;
import java.util.logging.Logger;

public class UserRestController {

  private static final Logger LOG = Logger.getLogger(UserRestController.class.getName());

  private final CreateRequesterUseCase createRequester;
  private final CreateAssigneeUseCase createAssignee;
  private final ChangeAgentAvailabilityUseCase changeAvailability;
  private final ChangeEmailUseCase changeEmail;
  private final AssignAgentToTeamUseCase assignToTeam;
  private final RemoveAgentFromTeamUseCase removeFromTeam;
  private final CreateSupportTeamUseCase createTeam;
  private final GetAvailableAgentsUseCase getAgents;

  public UserRestController(
      CreateRequesterUseCase createRequester,
      CreateAssigneeUseCase createAssignee,
      ChangeAgentAvailabilityUseCase changeAvailability,
      ChangeEmailUseCase changeEmail,
      AssignAgentToTeamUseCase assignToTeam,
      RemoveAgentFromTeamUseCase removeFromTeam,
      CreateSupportTeamUseCase createTeam,
      GetAvailableAgentsUseCase getAgents) {
    this.createRequester = createRequester;
    this.createAssignee = createAssignee;
    this.changeAvailability = changeAvailability;
    this.changeEmail = changeEmail;
    this.assignToTeam = assignToTeam;
    this.removeFromTeam = removeFromTeam;
    this.createTeam = createTeam;
    this.getAgents = getAgents;
  }

  public void configureRoutes() {
    ApiBuilder.path(
        "/api/users",
        () -> {
          ApiBuilder.post("/requesters", this::handleCreateRequester);
          ApiBuilder.post("/agents", this::handleCreateAssignee);
          ApiBuilder.path(
              "/agents/{id}",
              () -> ApiBuilder.patch("/availability", this::handleChangeAvailability));
          ApiBuilder.path(
              "/requesters/{id}", () -> ApiBuilder.patch("/email", this::handleChangeEmail));
        });

    ApiBuilder.path(
        "/api/teams",
        () -> {
          ApiBuilder.post(this::handleCreateTeam);
          ApiBuilder.path(
              "/{teamId}",
              () -> {
                ApiBuilder.post("/agents", this::handleAssignAgent);
                ApiBuilder.delete("/agents/{agentId}", this::handleRemoveAgent);
              });
        });

    ApiBuilder.path(
        "/internal/users", () -> ApiBuilder.get("/agents", this::handleGetAvailableAgents));
  }

  private void handleCreateRequester(Context ctx) {
    CreateRequesterRequest body = ctx.bodyAsClass(CreateRequesterRequest.class);
    if (body.email() == null || body.email().isBlank()) {
      ctx.status(400).result("email must not be blank");
      return;
    }
    UserID id = createRequester.createRequester(body.email());
    ctx.status(201).json(new IdResponse(id.id()));
  }

  private void handleCreateAssignee(Context ctx) {
    CreateAssigneeRequest body = ctx.bodyAsClass(CreateAssigneeRequest.class);
    AgentStatus status =
        body.agentStatus() != null
            ? AgentStatus.valueOf(body.agentStatus())
            : AgentStatus.AVAILABLE;
    UserID id = createAssignee.createAssignee(body.agentId(), status);
    ctx.status(201).json(new IdResponse(id.id()));
  }

  private void handleChangeAvailability(Context ctx) {
    String id = ctx.pathParam("id");
    AvailabilityRequest body = ctx.bodyAsClass(AvailabilityRequest.class);
    changeAvailability.changeAgentStatus(new UserID(id), AgentStatus.valueOf(body.agentStatus()));
    ctx.status(200);
  }

  private void handleChangeEmail(Context ctx) {
    String id = ctx.pathParam("id");
    EmailRequest body = ctx.bodyAsClass(EmailRequest.class);
    changeEmail.changeEmail(new UserID(id), new EmailAddress(body.email()));
    ctx.status(200);
  }

  private void handleCreateTeam(Context ctx) {
    CreateTeamRequest body = ctx.bodyAsClass(CreateTeamRequest.class);
    SupportTeamID teamId = createTeam.createTeam(body.name(), body.areaOfInterest());
    ctx.status(201).json(new IdResponse(teamId.id()));
  }

  private void handleAssignAgent(Context ctx) {
    String teamId = ctx.pathParam("teamId");
    AssignAgentRequest body = ctx.bodyAsClass(AssignAgentRequest.class);
    assignToTeam.assignAgentToTeam(new UserID(body.agentId()), new SupportTeamID(teamId));
    ctx.status(200);
  }

  private void handleRemoveAgent(Context ctx) {
    String teamId = ctx.pathParam("teamId");
    String agentId = ctx.pathParam("agentId");
    removeFromTeam.removeAgentFromTeam(new UserID(agentId), new SupportTeamID(teamId));
    ctx.status(200);
  }

  private void handleGetAvailableAgents(Context ctx) {
    String specialization = ctx.queryParam("specialization");
    if (specialization == null || specialization.isBlank()) {
      ctx.status(400).result("Query param 'specialization' is required");
      return;
    }
    List<AgentSnapshot> snapshots = getAgents.getAvailableAgentsForArea(specialization);
    List<AgentSnapshotResponse> response =
        snapshots.stream()
            .map(s -> new AgentSnapshotResponse(s.agentId(), 0, s.available()))
            .toList();
    ctx.status(200).json(response);
  }

  record CreateRequesterRequest(String email) {}

  record CreateAssigneeRequest(String agentId, String agentStatus) {}

  record AvailabilityRequest(String agentStatus) {}

  record EmailRequest(String email) {}

  record CreateTeamRequest(String name, String areaOfInterest) {}

  record AssignAgentRequest(String agentId) {}

  record IdResponse(String id) {}

  record AgentSnapshotResponse(String agentId, int currentLoad, boolean available) {}
}
