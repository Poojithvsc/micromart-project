package com.micromart.user.event;

import com.micromart.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * User Event Publisher - Publishes domain events for user operations.
 * <p>
 * Spring: ApplicationEventPublisher
 * Central interface for publishing application events.
 * Events can be synchronous or asynchronous depending on listener configuration.
 * <p>
 * This class encapsulates event creation and publishing logic,
 * keeping service classes clean and focused on business logic.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Publish user registered event.
     *
     * @param user The newly registered user
     */
    public void publishUserRegistered(User user) {
        UserRegisteredEvent event = new UserRegisteredEvent(
                this,
                user.getId(),
                user.getUsername(),
                user.getEmail().getValue(),
                user.getFullName()
        );

        log.debug("Publishing UserRegisteredEvent for user: {}", user.getUsername());
        eventPublisher.publishEvent(event);
    }

    /**
     * Publish user logged in event.
     *
     * @param user      The logged in user
     * @param ipAddress Client IP address
     * @param userAgent Client user agent
     */
    public void publishUserLoggedIn(User user, String ipAddress, String userAgent) {
        UserLoggedInEvent event = new UserLoggedInEvent(
                this,
                user.getId(),
                user.getUsername(),
                ipAddress,
                userAgent
        );

        log.debug("Publishing UserLoggedInEvent for user: {}", user.getUsername());
        eventPublisher.publishEvent(event);
    }

    /**
     * Publish account locked event.
     *
     * @param user   The locked user
     * @param reason Reason for locking
     */
    public void publishAccountLocked(User user, String reason) {
        UserAccountStatusEvent event = new UserAccountStatusEvent(
                this,
                user.getId(),
                user.getUsername(),
                UserEvent.UserEventType.ACCOUNT_LOCKED,
                reason,
                "SYSTEM"
        );

        log.debug("Publishing AccountLockedEvent for user: {}", user.getUsername());
        eventPublisher.publishEvent(event);
    }

    /**
     * Publish account unlocked event.
     *
     * @param user        The unlocked user
     * @param triggeredBy Admin who unlocked the account
     */
    public void publishAccountUnlocked(User user, String triggeredBy) {
        UserAccountStatusEvent event = new UserAccountStatusEvent(
                this,
                user.getId(),
                user.getUsername(),
                UserEvent.UserEventType.ACCOUNT_UNLOCKED,
                "Admin action",
                triggeredBy
        );

        log.debug("Publishing AccountUnlockedEvent for user: {}", user.getUsername());
        eventPublisher.publishEvent(event);
    }

    /**
     * Publish account disabled event.
     *
     * @param user        The disabled user
     * @param reason      Reason for disabling
     * @param triggeredBy Admin who disabled the account
     */
    public void publishAccountDisabled(User user, String reason, String triggeredBy) {
        UserAccountStatusEvent event = new UserAccountStatusEvent(
                this,
                user.getId(),
                user.getUsername(),
                UserEvent.UserEventType.ACCOUNT_DISABLED,
                reason,
                triggeredBy
        );

        log.debug("Publishing AccountDisabledEvent for user: {}", user.getUsername());
        eventPublisher.publishEvent(event);
    }

    /**
     * Publish account enabled event.
     *
     * @param user        The enabled user
     * @param triggeredBy Admin who enabled the account
     */
    public void publishAccountEnabled(User user, String triggeredBy) {
        UserAccountStatusEvent event = new UserAccountStatusEvent(
                this,
                user.getId(),
                user.getUsername(),
                UserEvent.UserEventType.ACCOUNT_ENABLED,
                "Admin action",
                triggeredBy
        );

        log.debug("Publishing AccountEnabledEvent for user: {}", user.getUsername());
        eventPublisher.publishEvent(event);
    }
}
