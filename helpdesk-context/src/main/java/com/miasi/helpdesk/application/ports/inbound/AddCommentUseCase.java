package com.miasi.helpdesk.application.ports.inbound;

import com.miasi.helpdesk.application.domain.model.TicketID;

public interface AddCommentUseCase {
  void execute(TicketID ticketId, String authorId, String content);
}
