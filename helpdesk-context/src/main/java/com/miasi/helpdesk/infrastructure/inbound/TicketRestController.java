package com.miasi.helpdesk.infrastructure.inbound;

import com.miasi.helpdesk.application.domain.model.TicketID;
import com.miasi.helpdesk.application.ports.inbound.AddCommentUseCase;
import com.miasi.helpdesk.application.ports.inbound.AssignTicketUseCase;
import com.miasi.helpdesk.application.ports.inbound.CreateTicketUseCase;
import com.miasi.helpdesk.application.ports.inbound.GetTicketUseCase;
import com.miasi.helpdesk.application.ports.inbound.ResolveTicketUseCase;
import com.miasi.helpdesk.application.ports.inbound.TicketView;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.http.Context;

public class TicketRestController {

  private final CreateTicketUseCase createTicketUseCase;
  private final AssignTicketUseCase assignTicketUseCase;
  private final ResolveTicketUseCase resolveTicketUseCase;
  private final AddCommentUseCase addCommentUseCase;
  private final GetTicketUseCase getTicketUseCase;

  public TicketRestController(
      CreateTicketUseCase createTicketUseCase,
      AssignTicketUseCase assignTicketUseCase,
      ResolveTicketUseCase resolveTicketUseCase,
      AddCommentUseCase addCommentUseCase,
      GetTicketUseCase getTicketUseCase) {
    this.createTicketUseCase = createTicketUseCase;
    this.assignTicketUseCase = assignTicketUseCase;
    this.resolveTicketUseCase = resolveTicketUseCase;
    this.addCommentUseCase = addCommentUseCase;
    this.getTicketUseCase = getTicketUseCase;
  }

  /** Called inside {@code config.routes.apiBuilder()} during Javalin initialisation. */
  public void configureRoutes() {
    ApiBuilder.path(
        "/api/tickets",
        () -> {
          ApiBuilder.post(this::handleCreateTicket);
          ApiBuilder.path(
              "/{id}",
              () -> {
                ApiBuilder.get(this::handleGetTicket);
                ApiBuilder.post("/assign", this::handleAssignTicket);
                ApiBuilder.post("/resolve", this::handleResolveTicket);
                ApiBuilder.post("/comments", this::handleAddComment);
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

  private void handleGetTicket(Context ctx) {
    String id = ctx.pathParam("id");
    TicketView view = getTicketUseCase.get(new TicketID(id));
    ctx.status(200).json(view);
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

  private void handleAddComment(Context ctx) {
    String id = ctx.pathParam("id");
    AddCommentRequest body = ctx.bodyAsClass(AddCommentRequest.class);
    addCommentUseCase.execute(new TicketID(id), body.authorId(), body.content());
    ctx.status(201);
  }

  record CreateTicketRequest(String title, String description, String requesterId) {}

  record TicketCreatedResponse(String ticketId) {}

  record ResolveRequest(String resolution) {}

  record AddCommentRequest(String authorId, String content) {}
}
