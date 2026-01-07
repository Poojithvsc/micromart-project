package com.micromart.user.service.notification;

/**
 * Notification Service Interface.
 * <p>
 * PEAA Pattern: Separated Interface
 * Multiple implementations demonstrate @Primary and @Qualifier usage.
 * <p>
 * This interface has multiple implementations:
 * - EmailNotificationService (@Primary - default)
 * - SmsNotificationService (@Qualifier for specific injection)
 * - MockNotificationService (@Conditional - only in dev profile)
 */
public interface NotificationService {

    /**
     * Send a notification to a user.
     *
     * @param recipient The recipient (email, phone, etc.)
     * @param subject   Notification subject
     * @param message   Notification message
     * @return true if notification was sent successfully
     */
    boolean sendNotification(String recipient, String subject, String message);

    /**
     * Get the notification channel type.
     *
     * @return Channel type (EMAIL, SMS, MOCK)
     */
    String getChannelType();
}
