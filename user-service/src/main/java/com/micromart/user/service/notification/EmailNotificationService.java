package com.micromart.user.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Email Notification Service Implementation.
 * <p>
 * Spring Annotation: @Primary
 * Marks this implementation as the default when multiple beans
 * of the same type exist. When NotificationService is injected
 * without @Qualifier, this bean will be used.
 * <p>
 * Spring Annotation: @Value
 * Demonstrates property injection for email configuration.
 */
@Service
@Primary
@Slf4j
public class EmailNotificationService implements NotificationService {

    /**
     * Email sender address injected from properties.
     * Demonstrates @Value with default value.
     */
    @Value("${notification.email.from:noreply@micromart.com}")
    private String fromAddress;

    /**
     * SMTP host configuration.
     */
    @Value("${notification.email.smtp-host:localhost}")
    private String smtpHost;

    /**
     * SMTP port with type conversion.
     */
    @Value("${notification.email.smtp-port:587}")
    private int smtpPort;

    /**
     * Email enabled flag - demonstrates boolean property.
     */
    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    @Override
    public boolean sendNotification(String recipient, String subject, String message) {
        if (!emailEnabled) {
            log.warn("Email notifications are disabled");
            return false;
        }

        log.info("Sending EMAIL notification:");
        log.info("  From: {}", fromAddress);
        log.info("  To: {}", recipient);
        log.info("  Subject: {}", subject);
        log.info("  SMTP: {}:{}", smtpHost, smtpPort);

        // In real implementation, send email via JavaMailSender
        // For demo, just log the action
        return true;
    }

    @Override
    public String getChannelType() {
        return "EMAIL";
    }
}
