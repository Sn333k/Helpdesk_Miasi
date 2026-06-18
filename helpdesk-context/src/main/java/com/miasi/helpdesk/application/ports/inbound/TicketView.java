package com.miasi.helpdesk.application.ports.inbound;

import java.time.Instant;
import java.util.List;

public record TicketView(
    String ticketId,
    String title,
    String description,
    String status,
    String priority,
    String requesterId,
    String assigneeId,
    String category,
    Instant slaDeadline,
    List<CommentView> comments) {

  public record CommentView(String authorId, String content, Instant createdAt) {}
}
