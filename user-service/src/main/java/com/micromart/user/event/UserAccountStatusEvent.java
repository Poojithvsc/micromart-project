package com.micromart.user.event;

import lombok.Getter;

/**
 * User Account Status Event - Published when account status changes.
 * <p>
 * Covers events like:
 * - Account locked (too many failed attempts)
 * - Account unlocked (admin action)
 * - Account disabled/enabled
 * <p>
 * Listeners can react to:
 * - Send notification to user
 * - Alert security team
 * - Log for compliance audit
 */
@Getter
public class UserAccountStatusEvent extends UserEvent {

    private final String reason;
    private final String triggeredBy; // "SYSTEM" or admin username

    public UserAccountStatusEvent(Object source, Long userId, String username,
                                   UserEventType eventType, String reason, String triggeredBy) {
        super(source, userId, username, eventType);
        this.reason = reason;
        this.triggeredBy = triggeredBy;
    }

    @Override
    public String toString() {
        return String.format("UserAccountStatusEvent[userId=%d, type=%s, reason=%s, by=%s]",
                getUserId(), getEventType(), reason, triggeredBy);
    }
}
