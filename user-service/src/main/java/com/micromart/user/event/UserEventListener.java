package com.micromart.user.event;

import com.micromart.user.domain.User;
import com.micromart.user.repository.UserRepository;
import com.micromart.user.service.UserNotificationFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * User Event Listener - Handles all user-related domain events.
 * <p>
 * Spring Annotations:
 * - @EventListener: Marks methods as event handlers
 * - @TransactionalEventListener: Event listener that runs in transaction context
 * - @Async: Executes handler asynchronously
 * <p>
 * TransactionPhase options:
 * - BEFORE_COMMIT: Execute before transaction commits
 * - AFTER_COMMIT: Execute after successful commit (default)
 * - AFTER_ROLLBACK: Execute after transaction rollback
 * - AFTER_COMPLETION: Execute after transaction completes (success or rollback)
 * <p>
 * Best Practices:
 * - Use @TransactionalEventListener(phase = AFTER_COMMIT) for side effects
 * - Use @Async for non-blocking operations
 * - Keep event handlers focused and lightweight
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {

    private final UserNotificationFacade notificationFacade;
    private final UserRepository userRepository;

    /**
     * Handle user registration event.
     * <p>
     * @TransactionalEventListener: Only fires after transaction commits successfully.
     * This ensures we only send welcome email if registration actually succeeded.
     * <p>
     * @Async: Sends notification in background thread to not block registration response.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Handling UserRegisteredEvent: {}", event);

        try {
            // Find user and send welcome notification
            userRepository.findById(event.getUserId()).ifPresent(user -> {
                log.info("Sending welcome notification to user: {}", user.getUsername());
                notificationFacade.sendWelcomeNotification(user);
            });

            // Additional async operations could go here:
            // - Initialize user preferences
            // - Sync to analytics system
            // - Create default cart/wishlist

        } catch (Exception e) {
            log.error("Error handling UserRegisteredEvent: {}", e.getMessage(), e);
            // Don't rethrow - event handling failures shouldn't affect main flow
        }
    }

    /**
     * Handle user login event.
     * <p>
     * @EventListener: Simple event listener, doesn't require transaction.
     * Used for logging and audit purposes.
     */
    @EventListener
    public void handleUserLoggedIn(UserLoggedInEvent event) {
        log.info("Handling UserLoggedInEvent: {}", event);

        // Log for security audit
        log.info("User {} logged in from IP: {}, UserAgent: {}",
                event.getUsername(),
                event.getIpAddress(),
                event.getUserAgent());

        // Could also:
        // - Check for suspicious login patterns
        // - Update login statistics
        // - Send login notification for new device
    }

    /**
     * Handle account status change events.
     * <p>
     * Uses @TransactionalEventListener to ensure database changes are committed
     * before sending notifications.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleAccountStatusChange(UserAccountStatusEvent event) {
        log.info("Handling UserAccountStatusEvent: {}", event);

        userRepository.findById(event.getUserId()).ifPresent(user -> {
            switch (event.getEventType()) {
                case ACCOUNT_LOCKED -> handleAccountLocked(user, event);
                case ACCOUNT_UNLOCKED -> handleAccountUnlocked(user, event);
                case ACCOUNT_DISABLED -> handleAccountDisabled(user, event);
                case ACCOUNT_ENABLED -> handleAccountEnabled(user, event);
                default -> log.warn("Unhandled event type: {}", event.getEventType());
            }
        });
    }

    private void handleAccountLocked(User user, UserAccountStatusEvent event) {
        log.warn("Account locked for user: {}, reason: {}", user.getUsername(), event.getReason());

        // Send notification about account lock
        notificationFacade.sendMultiChannelNotification(
                user,
                "Account Locked",
                String.format("Your MicroMart account has been locked. Reason: %s. " +
                        "Please contact support if you need assistance.", event.getReason())
        );
    }

    private void handleAccountUnlocked(User user, UserAccountStatusEvent event) {
        log.info("Account unlocked for user: {}, by: {}", user.getUsername(), event.getTriggeredBy());

        notificationFacade.sendMultiChannelNotification(
                user,
                "Account Unlocked",
                "Your MicroMart account has been unlocked. You can now log in."
        );
    }

    private void handleAccountDisabled(User user, UserAccountStatusEvent event) {
        log.warn("Account disabled for user: {}, reason: {}", user.getUsername(), event.getReason());

        notificationFacade.sendMultiChannelNotification(
                user,
                "Account Disabled",
                String.format("Your MicroMart account has been disabled. Reason: %s", event.getReason())
        );
    }

    private void handleAccountEnabled(User user, UserAccountStatusEvent event) {
        log.info("Account enabled for user: {}", user.getUsername());

        notificationFacade.sendMultiChannelNotification(
                user,
                "Account Enabled",
                "Your MicroMart account has been enabled. Welcome back!"
        );
    }
}
