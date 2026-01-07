package com.micromart.user.event;

import lombok.Getter;

/**
 * User Registered Event - Published when a new user registers.
 * <p>
 * Spring: ApplicationEvent (via parent)
 * Can be published using ApplicationEventPublisher.
 * <p>
 * Listeners can react to:
 * - Send welcome email
 * - Initialize user preferences
 * - Log for analytics
 * - Sync to external systems
 */
@Getter
public class UserRegisteredEvent extends UserEvent {

    private final String email;
    private final String fullName;

    public UserRegisteredEvent(Object source, Long userId, String username, String email, String fullName) {
        super(source, userId, username, UserEventType.REGISTERED);
        this.email = email;
        this.fullName = fullName;
    }

    @Override
    public String toString() {
        return String.format("UserRegisteredEvent[userId=%d, username=%s, email=%s]",
                getUserId(), getUsername(), email);
    }
}
