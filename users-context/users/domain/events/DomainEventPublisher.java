package com.miasi.users.domain.events;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Simple synchronous domain event publisher.
 * Aggregates collect events internally; the application layer
 * flushes them through this publisher after each use-case execution.
 *
 * Usage:
 *   DomainEventPublisher publisher = DomainEventPublisher.instance();
 *   publisher.subscribe(AgentAvailabilityChanged.class, event -> ...);
 */
public final class DomainEventPublisher {

    private static final DomainEventPublisher INSTANCE = new DomainEventPublisher();

    private final List<EventSubscription<?>> subscriptions = new ArrayList<>();

    private DomainEventPublisher() {}

    public static DomainEventPublisher instance() {
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> void subscribe(Class<T> eventType, Consumer<T> handler) {
        subscriptions.add(new EventSubscription<>(eventType, handler));
    }

    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> void publish(T event) {
        if (event == null) return;
        for (EventSubscription<?> subscription : subscriptions) {
            if (subscription.eventType().isAssignableFrom(event.getClass())) {
                ((Consumer<T>) subscription.handler()).accept(event);
            }
        }
    }

    public void publishAll(List<DomainEvent> events) {
        if (events == null) return;
        events.forEach(this::publish);
    }

    /** For testing: reset all subscribers. */
    public void reset() {
        subscriptions.clear();
    }

    private record EventSubscription<T extends DomainEvent>(
            Class<T> eventType,
            Consumer<T> handler) {}
}
