package com.miasi.users.domain.model;

import com.miasi.users.domain.events.DomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root: Requester
 *
 * Represents a user who submits tickets.
 * Maintains account lifecycle: ACTIVE <-> SUSPENDED.
 *
 * Invariants:
 *   - A SUSPENDED requester cannot create new tickets.
 *   - currentOpen (number of open tickets) must never be negative.
 *   - UserID is immutable throughout lifecycle.
 */
public class Requester {

    private final UserID userId;
    private EmailAddress email;
    private AccountStatus accountStatus;
    private int currentOpen;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // Package-private: use RequesterFactory
    Requester(UserID userId, EmailAddress email) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(email, "email must not be null");
        this.userId = userId;
        this.email = email;
        this.accountStatus = AccountStatus.ACTIVE;
        this.currentOpen = 0;
    }

    // -------------------------------------------------------------------------
    // Commands
    // -------------------------------------------------------------------------

    /**
     * Creates a ticket request. Returns a ticket creation token.
     *
     * @throws IllegalStateException if account is suspended
     */
    public void createTicket(String title, String description) {
        if (!accountStatus.isActive()) {
            throw new IllegalStateException(
                    "Suspended requester " + userId + " cannot create tickets");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Ticket title must not be blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Ticket description must not be blank");
        }
        currentOpen++;
        // Ticket creation is handled by Ticket context — no domain event needed here
    }

    /**
     * Notifies that one of this requester's tickets was closed/resolved.
     */
    public void ticketClosed() {
        if (currentOpen > 0) currentOpen--;
    }

    /**
     * Suspends the requester account.
     */
    public void suspend() {
        this.accountStatus = accountStatus.suspend();
    }

    /**
     * Reactivates the requester account.
     */
    public void activate() {
        this.accountStatus = accountStatus.activate();
    }

    /**
     * Updates the requester's email address.
     */
    public void changeEmail(EmailAddress newEmail) {
        Objects.requireNonNull(newEmail, "newEmail must not be null");
        this.email = newEmail;
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    public boolean canCreateTickets() {
        return accountStatus.isActive();
    }

    public UserID getUserId() { return userId; }
    public EmailAddress getEmail() { return email; }
    public AccountStatus getAccountStatus() { return accountStatus; }
    public int getCurrentOpen() { return currentOpen; }

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
        if (!(o instanceof Requester)) return false;
        Requester requester = (Requester) o;
        return Objects.equals(userId, requester.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "Requester{userId=" + userId
                + ", accountStatus=" + accountStatus
                + ", currentOpen=" + currentOpen + "}";
    }
}
