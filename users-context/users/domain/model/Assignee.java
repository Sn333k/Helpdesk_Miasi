package com.miasi.users.domain.model;

import com.miasi.users.domain.events.AgentAvailabilityChanged;
import com.miasi.users.domain.events.DomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root: Assignee
 *
 * Represents an agent (helpdesk employee) who resolves tickets.
 * Maintains two independent state machines:
 *   - Account status:     ACTIVE <-> SUSPENDED
 *   - Availability:       AVAILABLE <-> BUSY <-> UNAVAILABLE
 *
 * Invariants:
 *   - A SUSPENDED agent cannot change availability.
 *   - An UNAVAILABLE agent cannot take new tickets.
 *   - currentLoad must never be negative.
 */
public class Assignee {

    private final UserID userId;
    private EmailAddress email;
    private AccountStatus accountStatus;
    private AgentStatus agentStatus;
    private int currentLoad;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // Package-private: use AssigneeFactory
    Assignee(UserID userId, EmailAddress email) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(email, "email must not be null");
        this.userId = userId;
        this.email = email;
        this.accountStatus = AccountStatus.ACTIVE;
        this.agentStatus = AgentStatus.AVAILABLE;
        this.currentLoad = 0;
    }

    // -------------------------------------------------------------------------
    // Commands
    // -------------------------------------------------------------------------

    /**
     * Changes the agent's availability status.
     * Raises AgentAvailabilityChanged event.
     *
     * @throws IllegalStateException if account is suspended
     */
    public void changeAvailability(AgentStatus newStatus) {
        if (!accountStatus.isActive()) {
            throw new IllegalStateException(
                    "Cannot change availability for a suspended agent: " + userId);
        }
        this.agentStatus = this.agentStatus.transitionTo(newStatus);
        domainEvents.add(new AgentAvailabilityChanged(userId, this.agentStatus));
    }

    /**
     * Increments the agent's current ticket load (called when a ticket is assigned).
     *
     * @throws IllegalStateException if agent is not available
     */
    public void incrementLoad() {
        if (!agentStatus.canTakeNewTicket()) {
            throw new IllegalStateException(
                    "Agent " + userId + " cannot take new tickets (status: " + agentStatus + ")");
        }
        currentLoad++;
        if (currentLoad >= 5) {
            // Threshold: agent becomes BUSY when load reaches 5
            AgentStatus previous = this.agentStatus;
            this.agentStatus = AgentStatus.BUSY;
            if (previous != AgentStatus.BUSY) {
                domainEvents.add(new AgentAvailabilityChanged(userId, this.agentStatus));
            }
        }
    }

    /**
     * Decrements the agent's ticket load (called when a ticket is resolved/reassigned).
     */
    public void decrementLoad() {
        if (currentLoad <= 0) {
            currentLoad = 0;
            return;
        }
        currentLoad--;
        if (currentLoad < 5 && agentStatus == AgentStatus.BUSY) {
            this.agentStatus = AgentStatus.AVAILABLE;
            domainEvents.add(new AgentAvailabilityChanged(userId, this.agentStatus));
        }
    }

    /**
     * Suspends the agent account. Automatically marks agent as UNAVAILABLE.
     */
    public void suspend() {
        this.accountStatus = accountStatus.suspend();
        if (this.agentStatus != AgentStatus.UNAVAILABLE) {
            this.agentStatus = AgentStatus.UNAVAILABLE;
            domainEvents.add(new AgentAvailabilityChanged(userId, AgentStatus.UNAVAILABLE));
        }
    }

    /**
     * Reactivates a suspended agent account.
     */
    public void activate() {
        this.accountStatus = accountStatus.activate();
        this.agentStatus = AgentStatus.AVAILABLE;
        domainEvents.add(new AgentAvailabilityChanged(userId, AgentStatus.AVAILABLE));
    }

    /**
     * Updates the agent's email address.
     */
    public void changeEmail(EmailAddress newEmail) {
        Objects.requireNonNull(newEmail, "newEmail must not be null");
        this.email = newEmail;
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    public boolean isAvailable() {
        return accountStatus.isActive() && agentStatus.canTakeNewTicket();
    }

    public UserID getUserId() { return userId; }
    public EmailAddress getEmail() { return email; }
    public AccountStatus getAccountStatus() { return accountStatus; }
    public AgentStatus getAgentStatus() { return agentStatus; }
    public int getCurrentLoad() { return currentLoad; }

    // -------------------------------------------------------------------------
    // Event handling
    // -------------------------------------------------------------------------

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = Collections.unmodifiableList(new ArrayList<>(domainEvents));
        domainEvents.clear();
        return events;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Assignee)) return false;
        Assignee assignee = (Assignee) o;
        return Objects.equals(userId, assignee.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "Assignee{userId=" + userId
                + ", accountStatus=" + accountStatus
                + ", agentStatus=" + agentStatus
                + ", currentLoad=" + currentLoad + "}";
    }
}
