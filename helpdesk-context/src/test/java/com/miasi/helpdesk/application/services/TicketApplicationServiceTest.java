package com.miasi.helpdesk.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.miasi.helpdesk.application.domain.events.DomainEvent;
import com.miasi.helpdesk.application.domain.events.SLABreached;
import com.miasi.helpdesk.application.domain.events.TicketAssigned;
import com.miasi.helpdesk.application.domain.events.TicketCreated;
import com.miasi.helpdesk.application.domain.model.AgentSnapshot;
import com.miasi.helpdesk.application.domain.model.AssigneeID;
import com.miasi.helpdesk.application.domain.model.Category;
import com.miasi.helpdesk.application.domain.model.RequesterID;
import com.miasi.helpdesk.application.domain.model.SLA;
import com.miasi.helpdesk.application.domain.model.Ticket;
import com.miasi.helpdesk.application.domain.model.TicketID;
import com.miasi.helpdesk.application.domain.model.TicketNotFoundException;
import com.miasi.helpdesk.application.domain.model.TicketPriority;
import com.miasi.helpdesk.application.domain.model.TicketStatus;
import com.miasi.helpdesk.application.domain.services.NoAvailableAgentException;
import com.miasi.helpdesk.application.domain.services.StaffAssignmentService;
import com.miasi.helpdesk.application.ports.outbound.IAgentAvailabilityProvider;
import com.miasi.helpdesk.application.ports.outbound.INotificationSender;
import com.miasi.helpdesk.application.ports.outbound.ITicketRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TicketApplicationServiceTest {

  private static final Category DEFAULT_CATEGORY = new Category("IT");
  private static final String REQUESTER_ID = "requester-1";
  private static final long SLA_MINUTES = 60L;

  private FakeTicketRepository repository;
  private CapturingNotificationSender notificationSender;
  private TicketApplicationService service;

  @BeforeEach
  void setUp() {
    repository = new FakeTicketRepository();
    notificationSender = new CapturingNotificationSender();
    service =
        new TicketApplicationService(
            new TicketFactory(SLA_MINUTES),
            repository,
            new SingleAgentProvider("agent-default"),
            notificationSender,
            new StaffAssignmentService(),
            DEFAULT_CATEGORY);
  }

  // ─── createTicket ─────────────────────────────────────────────────────────

  @Test
  void createTicket_shouldSaveTicketAndReturnNonNullId() {
    TicketID id = service.execute("My issue", "Details", REQUESTER_ID);

    assertThat(id).isNotNull();
    assertThat(repository.findById(id)).isPresent();
  }

  @Test
  void createTicket_savedTicket_shouldHaveStatusNew() {
    TicketID id = service.execute("My issue", "Details", REQUESTER_ID);

    Ticket ticket = repository.findById(id).orElseThrow();
    assertThat(ticket.getStatus()).isEqualTo(TicketStatus.NEW);
  }

  @Test
  void createTicket_shouldNotifyAboutTicketCreated() {
    service.execute("My issue", "Details", REQUESTER_ID);

    assertThat(notificationSender.getCaptured())
        .hasSize(1)
        .first()
        .isInstanceOf(TicketCreated.class);
  }

  // ─── assignTicket ─────────────────────────────────────────────────────────

  @Test
  void assignTicket_shouldTransitionStatusToAssigned() {
    TicketID id = service.execute("Issue", "Desc", REQUESTER_ID);
    notificationSender.clear();

    service.execute(id);

    Ticket ticket = repository.findById(id).orElseThrow();
    assertThat(ticket.getStatus()).isEqualTo(TicketStatus.ASSIGNED);
    assertThat(ticket.getAssigneeId()).isNotNull();
  }

  @Test
  void assignTicket_shouldSendAtLeastOneNotification() {
    TicketID id = service.execute("Issue", "Desc", REQUESTER_ID);
    notificationSender.clear();

    service.execute(id);

    assertThat(notificationSender.getCaptured())
        .isNotEmpty()
        .anyMatch(e -> e instanceof TicketAssigned);
  }

  @Test
  void assignTicket_noAgentsAvailable_shouldThrow() {
    TicketApplicationService noAgentService =
        new TicketApplicationService(
            new TicketFactory(SLA_MINUTES),
            repository,
            category -> List.of(),
            notificationSender,
            new StaffAssignmentService(),
            DEFAULT_CATEGORY);
    TicketID id = noAgentService.execute("Issue", "Desc", REQUESTER_ID);

    assertThatThrownBy(() -> noAgentService.execute(id))
        .isInstanceOf(NoAvailableAgentException.class);
  }

  @Test
  void assignTicket_notFound_shouldThrow() {
    TicketID missing = new TicketID("does-not-exist");

    assertThatThrownBy(() -> service.execute(missing)).isInstanceOf(TicketNotFoundException.class);
  }

  // ─── escalateTicket ───────────────────────────────────────────────────────

  @Test
  void escalateTicket_shouldRaisePriorityAboveMedium() {
    TicketID id = service.execute("Issue", "Desc", REQUESTER_ID);
    notificationSender.clear();

    service.escalate(id);

    Ticket ticket = repository.findById(id).orElseThrow();
    assertThat(ticket.getPriority().level()).isGreaterThan(TicketPriority.MEDIUM.level());
  }

  @Test
  void escalateTicket_whenSlaBreached_shouldNotifyAboutSLABreached() {
    SLA breachedSla = new SLA(Instant.now().minusSeconds(1));
    Ticket breachedTicket =
        Ticket.reconstitute(
            new TicketID("breach-1"),
            "Breached ticket",
            "Desc",
            new RequesterID(REQUESTER_ID),
            null,
            DEFAULT_CATEGORY,
            breachedSla,
            TicketPriority.MEDIUM,
            TicketStatus.NEW,
            List.of());
    repository.save(breachedTicket);
    notificationSender.clear();

    service.escalate(new TicketID("breach-1"));

    assertThat(notificationSender.getCaptured()).anyMatch(e -> e instanceof SLABreached);
  }

  @Test
  void escalateTicket_notFound_shouldThrow() {
    TicketID missing = new TicketID("ghost-ticket");

    assertThatThrownBy(() -> service.escalate(missing)).isInstanceOf(TicketNotFoundException.class);
  }

  // ─── resolveTicket ────────────────────────────────────────────────────────

  @Test
  void resolveTicket_shouldChangeStatusToResolved() {
    TicketID id = new TicketID("t-resolve");
    repository.save(inProgressTicket(id));
    notificationSender.clear();

    service.execute(id, "All done");

    Ticket ticket = repository.findById(id).orElseThrow();
    assertThat(ticket.getStatus()).isEqualTo(TicketStatus.RESOLVED);
    assertThat(ticket.getComments()).hasSize(1);
    assertThat(ticket.getComments().get(0).content()).isEqualTo("All done");
  }

  @Test
  void resolveTicket_shouldSendNotification() {
    TicketID id = new TicketID("t-notify");
    repository.save(inProgressTicket(id));
    notificationSender.clear();

    service.execute(id, "Fixed");

    assertThat(notificationSender.getCaptured()).isNotEmpty();
  }

  @Test
  void resolveTicket_notFound_shouldThrow() {
    TicketID missing = new TicketID("no-such-ticket");

    assertThatThrownBy(() -> service.execute(missing, "resolution"))
        .isInstanceOf(TicketNotFoundException.class);
  }

  // ─── Helpers ──────────────────────────────────────────────────────────────

  private static Ticket inProgressTicket(TicketID id) {
    return Ticket.reconstitute(
        id,
        "Title",
        "Desc",
        new RequesterID(REQUESTER_ID),
        new AssigneeID("agent-1"),
        DEFAULT_CATEGORY,
        new SLA(Instant.now().plusSeconds(3600)),
        TicketPriority.MEDIUM,
        TicketStatus.IN_PROGRESS,
        List.of());
  }

  // ─── Fakes ────────────────────────────────────────────────────────────────

  private static class FakeTicketRepository implements ITicketRepository {
    private final Map<String, Ticket> store = new HashMap<>();

    @Override
    public void save(Ticket ticket) {
      store.put(ticket.getId().id(), ticket);
    }

    @Override
    public Optional<Ticket> findById(TicketID id) {
      return Optional.ofNullable(store.get(id.id()));
    }

    @Override
    public List<Ticket> findAllBreachingSla(Instant now) {
      return store.values().stream().filter(t -> t.getSla().isBreached(now)).toList();
    }
  }

  private static class CapturingNotificationSender implements INotificationSender {
    private final List<DomainEvent> captured = new ArrayList<>();

    @Override
    public void sendNotification(DomainEvent event) {
      captured.add(event);
    }

    List<DomainEvent> getCaptured() {
      return List.copyOf(captured);
    }

    void clear() {
      captured.clear();
    }
  }

  private static class SingleAgentProvider implements IAgentAvailabilityProvider {
    private final String agentId;

    SingleAgentProvider(String agentId) {
      this.agentId = agentId;
    }

    @Override
    public List<AgentSnapshot> getAvailableAgentsForCategory(Category category) {
      return List.of(new AgentSnapshot(agentId, 1, true));
    }
  }
}
