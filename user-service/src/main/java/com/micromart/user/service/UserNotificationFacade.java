package com.micromart.user.service;

import com.micromart.user.domain.User;
import com.micromart.user.service.notification.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * User Notification Facade - Coordinates notification sending.
 * <p>
 * Demonstrates:
 * - @Autowired with @Qualifier for specific bean injection
 * - Using @Primary bean as default
 * - @Async for non-blocking notification sending
 * <p>
 * Spring Annotations:
 * - @Qualifier: Specifies which bean to inject when multiple exist
 * - @Async: Executes method asynchronously on a separate thread
 */
@Service
@Slf4j
public class UserNotificationFacade {

    /**
     * Primary notification service (Email).
     * Because EmailNotificationService has @Primary, it's injected without @Qualifier.
     */
    private final NotificationService primaryNotificationService;

    /**
     * SMS notification service.
     * Requires @Qualifier to inject specific implementation.
     */
    private final NotificationService smsNotificationService;

    /**
     * Constructor demonstrating @Qualifier usage.
     * <p>
     * First parameter: No @Qualifier - gets @Primary bean (Email)
     * Second parameter: @Qualifier("smsNotification") - gets SMS bean
     */
    @Autowired
    public UserNotificationFacade(
            NotificationService primaryNotificationService,
            @Qualifier("smsNotification") NotificationService smsNotificationService
    ) {
        this.primaryNotificationService = primaryNotificationService;
        this.smsNotificationService = smsNotificationService;

        log.info("UserNotificationFacade initialized:");
        log.info("  Primary channel: {}", primaryNotificationService.getChannelType());
        log.info("  SMS channel: {}", smsNotificationService.getChannelType());
    }

    /**
     * Send welcome notification to new user.
     * Uses primary (email) notification service.
     *
     * @param user Newly registered user
     */
    @Async
    public void sendWelcomeNotification(User user) {
        log.info("Sending welcome notification to: {}", user.getUsername());

        String subject = "Welcome to MicroMart!";
        String message = String.format(
                "Hello %s, welcome to MicroMart! Your account has been created successfully.",
                user.getFullName()
        );

        primaryNotificationService.sendNotification(
                user.getEmail().getValue(),
                subject,
                message
        );
    }

    /**
     * Send password reset notification.
     * Uses primary (email) notification service.
     *
     * @param user       User requesting reset
     * @param resetToken Password reset token
     */
    @Async
    public void sendPasswordResetNotification(User user, String resetToken) {
        log.info("Sending password reset notification to: {}", user.getUsername());

        String subject = "Password Reset Request";
        String message = String.format(
                "Hello %s, use this token to reset your password: %s",
                user.getFullName(),
                resetToken
        );

        primaryNotificationService.sendNotification(
                user.getEmail().getValue(),
                subject,
                message
        );
    }

    /**
     * Send SMS verification code.
     * Uses SMS notification service (via @Qualifier).
     *
     * @param phoneNumber Phone number to send to
     * @param code        Verification code
     */
    @Async
    public void sendSmsVerificationCode(String phoneNumber, String code) {
        log.info("Sending SMS verification code to: {}", phoneNumber);

        String message = String.format("Your MicroMart verification code is: %s", code);

        smsNotificationService.sendNotification(
                phoneNumber,
                null, // SMS doesn't use subject
                message
        );
    }

    /**
     * Send notification via both channels.
     * Demonstrates using multiple notification services.
     *
     * @param user    Target user
     * @param subject Notification subject
     * @param message Notification message
     */
    @Async
    public void sendMultiChannelNotification(User user, String subject, String message) {
        log.info("Sending multi-channel notification to: {}", user.getUsername());

        // Send email
        primaryNotificationService.sendNotification(
                user.getEmail().getValue(),
                subject,
                message
        );

        // Send SMS if phone number available
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank()) {
            smsNotificationService.sendNotification(
                    user.getPhoneNumber(),
                    subject,
                    message
            );
        }
    }
}
