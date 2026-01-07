package com.micromart.user.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mock Notification Service - For Development and Testing.
 * <p>
 * Spring Annotation: @Profile
 * This bean is ONLY created when the "dev" or "test" profile is active.
 * <p>
 * Spring Annotation: @ConditionalOnProperty
 * Additional condition - only created when notification.mock.enabled=true.
 * <p>
 * PEAA Pattern: Plugin
 * Different implementations are "plugged in" based on environment.
 * <p>
 * Usage:
 * - In dev profile with mock enabled, inject using @Qualifier("mockNotification")
 * - Useful for testing without sending real notifications
 * - Stores notifications in memory for verification
 */
@Service
@Qualifier("mockNotification")
@Profile({"dev", "test"})
@ConditionalOnProperty(
        name = "notification.mock.enabled",
        havingValue = "true",
        matchIfMissing = false
)
@Slf4j
public class MockNotificationService implements NotificationService {

    /**
     * In-memory storage of sent notifications.
     * Useful for testing verification.
     */
    private final List<MockNotification> sentNotifications = Collections.synchronizedList(new ArrayList<>());

    @Override
    public boolean sendNotification(String recipient, String subject, String message) {
        log.info("MOCK: Would send notification to {} - Subject: {}", recipient, subject);

        MockNotification notification = new MockNotification(recipient, subject, message);
        sentNotifications.add(notification);

        return true;
    }

    @Override
    public String getChannelType() {
        return "MOCK";
    }

    /**
     * Get all sent mock notifications.
     * Useful for test assertions.
     *
     * @return List of mock notifications
     */
    public List<MockNotification> getSentNotifications() {
        return new ArrayList<>(sentNotifications);
    }

    /**
     * Clear sent notifications.
     * Useful for test cleanup.
     */
    public void clearNotifications() {
        sentNotifications.clear();
    }

    /**
     * Check if a notification was sent to a recipient.
     *
     * @param recipient The recipient to check
     * @return true if notification was sent
     */
    public boolean wasNotificationSentTo(String recipient) {
        return sentNotifications.stream()
                .anyMatch(n -> n.recipient().equals(recipient));
    }

    /**
     * Record for storing mock notification data.
     */
    public record MockNotification(String recipient, String subject, String message) {
    }
}
