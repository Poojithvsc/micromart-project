package com.micromart.user.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Base User Event - Foundation for all user-related domain events.
 * <p>
 * Spring: ApplicationEvent
 * Extends Spring's ApplicationEvent to integrate with the event system.
 * <p>
 * PEAA Pattern: Domain Event
 * Domain events capture occurrences of business significance.
 * They enable loose coupling between components.
 */
@Getter
public abstract class UserEvent extends ApplicationEvent {

    private final Long userId;
    private final String username;
    private final UserEventType eventType;
    private final LocalDateTime occurredAt;

    protected UserEvent(Object source, Long userId, String username, UserEventType eventType) {
        super(source);
        this.userId = userId;
        this.username = username;
        this.eventType = eventType;
        this.occurredAt = LocalDateTime.now();
    }

    /**
     * Event types for user operations.
     */
    public enum UserEventType {
        REGISTERED,
        LOGGED_IN,
        LOGGED_OUT,
        PASSWORD_CHANGED,
        PROFILE_UPDATED,
        ACCOUNT_LOCKED,
        ACCOUNT_UNLOCKED,
        ACCOUNT_DISABLED,
        ACCOUNT_ENABLED
    }
}
