package com.micromart.user.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * SMS Notification Service Implementation.
 * <p>
 * Spring Annotation: @Qualifier
 * This bean can be injected using @Qualifier("smsNotification").
 * Without @Primary, this bean requires explicit qualification.
 * <p>
 * Usage example:
 * <pre>
 * {@code
 * @Autowired
 * @Qualifier("smsNotification")
 * private NotificationService smsService;
 * }
 * </pre>
 */
@Service
@Qualifier("smsNotification")
@Slf4j
public class SmsNotificationService implements NotificationService {

    /**
     * SMS provider API key.
     * Demonstrates @Value for sensitive configuration.
     */
    @Value("${notification.sms.api-key:demo-api-key}")
    private String apiKey;

    /**
     * SMS sender ID/number.
     */
    @Value("${notification.sms.sender-id:MICROMART}")
    private String senderId;

    /**
     * SMS enabled flag.
     */
    @Value("${notification.sms.enabled:false}")
    private boolean smsEnabled;

    @Override
    public boolean sendNotification(String recipient, String subject, String message) {
        if (!smsEnabled) {
            log.warn("SMS notifications are disabled");
            return false;
        }

        // SMS doesn't use subject, combine into message
        String smsMessage = subject != null ? subject + ": " + message : message;

        log.info("Sending SMS notification:");
        log.info("  From: {}", senderId);
        log.info("  To: {}", recipient);
        log.info("  Message: {}", smsMessage);

        // In real implementation, call SMS provider API
        // For demo, just log the action
        return true;
    }

    @Override
    public String getChannelType() {
        return "SMS";
    }
}
