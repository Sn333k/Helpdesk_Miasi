package com.miasi.helpdesk.infrastructure.inbound;

import com.miasi.helpdesk.application.ports.inbound.AssignTicketUseCase;
import com.miasi.helpdesk.application.ports.inbound.CreateTicketUseCase;
import com.miasi.helpdesk.application.ports.inbound.ResolveTicketUseCase;
import com.miasi.helpdesk.domain.model.TicketID;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.http.Context;

public class TicketRestController {

  private final CreateTicketUseCase createTicketUseCase;
  private final AssignTicketUseCase assignTicketUseCase;
  private final ResolveTicketUseCase resolveTicketUseCase;

  public TicketRestController(
      CreateTicketUseCase createTicketUseCase,
      AssignTicketUseCase assignTicketUseCase,
      ResolveTicketUseCase resolveTicketUseCase) {
    this.createTicketUseCase = createTicketUseCase;
    this.assignTicketUseCase = assignTicketUseCase;
    this.resolveTicketUseCase = resolveTicketUseCase;
  }

  /** Called inside {@code config.router.apiBuilder()} during Javalin initialisation. */
  public void configureRoutes() {
    ApiBuilder.path(
        "/api/tickets",
        () -> {
          ApiBuilder.post(this::handleCreateTicket);
          ApiBuilder.path(
              "/{id}",
              () -> {
                ApiBuilder.post("/assign", this::handleAssignTicket);
                ApiBuilder.post("/resolve", this::handleResolveTicket);
              });
        });
  }

  private void handleCreateTicket(Context ctx) {
    CreateTicketRequest body = ctx.bodyAsClass(CreateTicketRequest.class);
    if (body.title() == null || body.title().isBlank())
      ctx.status(400).result("title must not be blank");
    else if (body.requesterId() == null || body.requesterId().isBlank())
      ctx.status(400).result("requesterId must not be blank");
    else {
      TicketID ticketId =
          createTicketUseCase.execute(body.title(), body.description(), body.requesterId());
      ctx.status(201).json(new TicketCreatedResponse(ticketId.id()));
    }
  }

  private void handleAssignTicket(Context ctx) {
    String id = ctx.pathParam("id");
    assignTicketUseCase.execute(new TicketID(id));
    ctx.status(200);
  }

  private void handleResolveTicket(Context ctx) {
    String id = ctx.pathParam("id");
    ResolveRequest body = ctx.bodyAsClass(ResolveRequest.class);
    resolveTicketUseCase.execute(new TicketID(id), body.resolution());
    ctx.status(200);
  }

  record CreateTicketRequest(String title, String description, String requesterId) {}

  record TicketCreatedResponse(String ticketId) {}

  record ResolveRequest(String resolution) {}
}
