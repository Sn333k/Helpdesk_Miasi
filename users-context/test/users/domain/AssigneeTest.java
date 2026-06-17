package com.miasi.users.domain;

import com.miasi.users.domain.events.AgentAvailabilityChanged;
import com.miasi.users.domain.events.DomainEvent;
import com.miasi.users.domain.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AssigneeTest {

    private Assignee createAssignee() {
        return AssigneeFactory.create("agent@example.com");
    }

    @Test
    void newAssignee_isActiveAndAvailable() {
        Assignee a = createAssignee();
        assertEquals(AccountStatus.ACTIVE, a.getAccountStatus());
        assertEquals(AgentStatus.AVAILABLE, a.getAgentStatus());
        assertTrue(a.isAvailable());
    }

    @Test
    void changeAvailability_toUnavailable_publishesEvent() {
        Assignee a = createAssignee();
        a.changeAvailability(AgentStatus.UNAVAILABLE);

        assertEquals(AgentStatus.UNAVAILABLE, a.getAgentStatus());
        List<DomainEvent> events = a.pullDomainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(AgentAvailabilityChanged.class, events.get(0));
        assertEquals(AgentStatus.UNAVAILABLE,
                ((AgentAvailabilityChanged) events.get(0)).getNewStatus());
    }

    @Test
    void changeAvailability_sameStatus_throwsException() {
        Assignee a = createAssignee();
        assertThrows(IllegalStateException.class,
                () -> a.changeAvailability(AgentStatus.AVAILABLE));
    }

    @Test
    void suspend_setsUnavailableAndPublishesEvent() {
        Assignee a = createAssignee();
        a.suspend();

        assertEquals(AccountStatus.SUSPENDED, a.getAccountStatus());
        assertEquals(AgentStatus.UNAVAILABLE, a.getAgentStatus());
        assertFalse(a.isAvailable());

        List<DomainEvent> events = a.pullDomainEvents();
        assertTrue(events.stream().anyMatch(e -> e instanceof AgentAvailabilityChanged av
                && av.getNewStatus() == AgentStatus.UNAVAILABLE));
    }

    @Test
    void suspendedAgent_cannotChangeAvailability() {
        Assignee a = createAssignee();
        a.suspend();
        a.pullDomainEvents(); // clear

        assertThrows(IllegalStateException.class,
                () -> a.changeAvailability(AgentStatus.AVAILABLE));
    }

    @Test
    void activate_afterSuspend_setsAvailableAndPublishesEvent() {
        Assignee a = createAssignee();
        a.suspend();
        a.pullDomainEvents();

        a.activate();
        assertEquals(AccountStatus.ACTIVE, a.getAccountStatus());
        assertEquals(AgentStatus.AVAILABLE, a.getAgentStatus());

        List<DomainEvent> events = a.pullDomainEvents();
        assertEquals(1, events.size());
        assertEquals(AgentStatus.AVAILABLE,
                ((AgentAvailabilityChanged) events.get(0)).getNewStatus());
    }

    @Test
    void incrementLoad_beyondThreshold_makesBusy() {
        Assignee a = createAssignee();
        a.pullDomainEvents();
        for (int i = 0; i < 5; i++) a.incrementLoad();

        assertEquals(AgentStatus.BUSY, a.getAgentStatus());
        assertFalse(a.isAvailable());
    }

    @Test
    void decrementLoad_belowThreshold_makesAvailable() {
        Assignee a = createAssignee();
        for (int i = 0; i < 5; i++) a.incrementLoad();
        a.pullDomainEvents();

        a.decrementLoad();
        assertEquals(AgentStatus.AVAILABLE, a.getAgentStatus());
    }

    @Test
    void pullDomainEvents_clearsEventList() {
        Assignee a = createAssignee();
        a.changeAvailability(AgentStatus.BUSY);
        assertFalse(a.pullDomainEvents().isEmpty());
        assertTrue(a.pullDomainEvents().isEmpty());
    }

    @Test
    void equalityBasedOnUserId() {
        Assignee a = createAssignee();
        Assignee b = AssigneeFactory.reconstitute(
                a.getUserId().getId(),
                "other@example.com",
                AccountStatus.ACTIVE,
                AgentStatus.AVAILABLE,
                0);
        assertEquals(a, b);
    }
}
