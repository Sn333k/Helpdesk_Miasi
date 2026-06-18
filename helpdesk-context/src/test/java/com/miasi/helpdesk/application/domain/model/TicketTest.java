package com.miasi.helpdesk.application.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.miasi.helpdesk.application.domain.events.DomainEvent;
import com.miasi.helpdesk.application.domain.events.SLABreached;
import com.miasi.helpdesk.application.domain.events.TicketAssigned;
import com.miasi.helpdesk.application.domain.events.TicketCreated;
import com.miasi.helpdesk.application.domain.events.TicketStatusChanged;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class TicketTest {

  private static final RequesterID REQUESTER = new RequesterID("requester-1");
  private static final Category CATEGORY = new Category("IT");
  private static final SLA FUTURE_SLA = new SLA(Instant.now().plusSeconds(3600));

  private Ticket newTicket() {
    return new Ticket(
        "Sample title", "Description", REQUESTER, CATEGORY, FUTURE_SLA, TicketPriority.MEDIUM);
  }

  private Ticket reconstitutedInProgress() {
    return Ticket.reconstitute(
        new TicketID("t-in-progress"),
        "Title",
        "Desc",
        REQUESTER,
        new AssigneeID("agent-1"),
        CATEGORY,
        FUTURE_SLA,
        TicketPriority.MEDIUM,
        TicketStatus.IN_PROGRESS,
        List.of());
  }

  // ─── Creation ────────────────────────────────────────────────────────────────

  @Test
  void createTicket_shouldHaveStatusNew() {
    Ticket ticket = newTicket();

    assertThat(ticket.getStatus()).isEqualTo(TicketStatus.NEW);
    assertThat(ticket.getId()).isNotNull();
  }

  @Test
  void createTicket_shouldEmitTicketCreatedEvent() {
    Ticket ticket = newTicket();

    List<DomainEvent> events = ticket.popEvents();

    assertThat(events).hasSize(1);
    assertThat(events.get(0)).isInstanceOf(TicketCreated.class);
  }

  @Test
  void createTicket_blankTitle_shouldThrow() {
    assertThatThrownBy(
            () ->
                new Ticket(
                    "   ", "Description", REQUESTER, CATEGORY, FUTURE_SLA, TicketPriority.MEDIUM))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Title");
  }

  @Test
  void createTicket_nullRequester_shouldThrow() {
    assertThatThrownBy(
            () ->
                new Ticket(
                    "Title", "Description", null, CATEGORY, FUTURE_SLA, TicketPriority.MEDIUM))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("RequesterID");
  }

  // ─── Assign ──────────────────────────────────────────────────────────────────

  @Test
  void assignTo_shouldChangeStatusToAssigned() {
    Ticket ticket = newTicket();
    AssigneeID agent = new AssigneeID("agent-1");

    ticket.assignTo(agent);

    assertThat(ticket.getStatus()).isEqualTo(TicketStatus.ASSIGNED);
    assertThat(ticket.getAssigneeId()).isEqualTo(agent);
  }

  @Test
  void assignTo_shouldEmitTicketAssignedAndStatusChangedEvents() {
    Ticket ticket = newTicket();
    ticket.popEvents(); // consume creation event
    AssigneeID agent = new AssigneeID("agent-1");

    ticket.assignTo(agent);
    List<DomainEvent> events = ticket.popEvents();

    assertThat(events).hasSize(2);
    assertThat(events).anyMatch(e -> e instanceof TicketAssigned);
    assertThat(events).anyMatch(e -> e instanceof TicketStatusChanged);
  }

  @Test
  void assignTo_null_shouldThrow() {
    Ticket ticket = newTicket();

    assertThatThrownBy(() -> ticket.assignTo(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("AssigneeID");
  }

  @Test
  void assignTo_assigneeEqualsRequester_shouldThrow() {
    Ticket ticket = newTicket();
    AssigneeID sameAsRequester = new AssigneeID(REQUESTER.id());

    assertThatThrownBy(() -> ticket.assignTo(sameAsRequester))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void assignTo_alreadyResolved_shouldThrow() {
    Ticket ticket = reconstitutedInProgress();
    ticket.resolve("Done");

    assertThatThrownBy(() -> ticket.assignTo(new AssigneeID("agent-2")))
        .isInstanceOf(IllegalStateException.class);
  }

  // ─── Resolve ─────────────────────────────────────────────────────────────────

  @Test
  void resolve_shouldChangeStatusToResolved() {
    Ticket ticket = reconstitutedInProgress();

    ticket.resolve("Issue fixed");

    assertThat(ticket.getStatus()).isEqualTo(TicketStatus.RESOLVED);
    assertThat(ticket.getComments()).hasSize(1);
    assertThat(ticket.getComments().get(0).content()).isEqualTo("Issue fixed");
  }

  @Test
  void resolve_nullResolution_shouldNotAddComment() {
    Ticket ticket = reconstitutedInProgress();

    ticket.resolve(null);

    assertThat(ticket.getStatus()).isEqualTo(TicketStatus.RESOLVED);
    assertThat(ticket.getComments()).isEmpty();
  }

  @Test
  void resolve_onNewTicket_shouldThrow() {
    Ticket ticket = newTicket();

    assertThatThrownBy(() -> ticket.resolve("Cannot resolve"))
        .isInstanceOf(IllegalStateException.class);
  }

  // ─── Escalate priority ───────────────────────────────────────────────────────

  @Test
  void escalatePriority_medium_shouldBecomeHigh() {
    Ticket ticket = newTicket(); // starts at MEDIUM

    ticket.escalatePriority();

    assertThat(ticket.getPriority()).isEqualTo(TicketPriority.HIGH);
  }

  @Test
  void escalatePriority_fromCritical_shouldRemainCritical() {
    Ticket ticket = newTicket(); // MEDIUM
    ticket.escalatePriority(); // HIGH
    ticket.escalatePriority(); // CRITICAL

    ticket.escalatePriority(); // stays CRITICAL

    assertThat(ticket.getPriority()).isEqualTo(TicketPriority.CRITICAL);
  }

  @Test
  void escalatePriority_whenSlaBreached_shouldEmitSLABreachedEvent() {
    SLA breachedSla = new SLA(Instant.now().minusSeconds(1));
    Ticket ticket =
        new Ticket("Title", "Desc", REQUESTER, CATEGORY, breachedSla, TicketPriority.MEDIUM);
    ticket.popEvents(); // consume TicketCreated

    ticket.escalatePriority();
    List<DomainEvent> events = ticket.popEvents();

    assertThat(events).anyMatch(e -> e instanceof SLABreached);
  }

  @Test
  void escalatePriority_whenResolved_shouldThrow() {
    Ticket ticket = reconstitutedInProgress();
    ticket.resolve("Done");

    assertThatThrownBy(ticket::escalatePriority).isInstanceOf(IllegalStateException.class);
  }

  // ─── Add comment ─────────────────────────────────────────────────────────────

  @Test
  void addComment_shouldAppendComment() {
    Ticket ticket = newTicket();

    ticket.addComment("agent-1", "Working on it");

    assertThat(ticket.getComments()).hasSize(1);
    assertThat(ticket.getComments().get(0).content()).isEqualTo("Working on it");
    assertThat(ticket.getComments().get(0).authorId()).isEqualTo("agent-1");
  }

  @Test
  void addComment_whenResolved_shouldThrow() {
    Ticket resolved =
        Ticket.reconstitute(
            new TicketID("t-resolved"),
            "Title",
            "Desc",
            REQUESTER,
            new AssigneeID("agent-1"),
            CATEGORY,
            FUTURE_SLA,
            TicketPriority.MEDIUM,
            TicketStatus.RESOLVED,
            List.of());

    assertThatThrownBy(() -> resolved.addComment("agent-1", "Late comment"))
        .isInstanceOf(IllegalStateException.class);
  }

  // ─── Pop events ──────────────────────────────────────────────────────────────

  @Test
  void popEvents_shouldClearQueueAfterFirstCall() {
    Ticket ticket = newTicket();

    List<DomainEvent> first = ticket.popEvents();
    List<DomainEvent> second = ticket.popEvents();

    assertThat(first).isNotEmpty();
    assertThat(second).isEmpty();
  }
}
