package com.micromart.user.event;

import lombok.Getter;

/**
 * User Logged In Event - Published when a user successfully logs in.
 * <p>
 * Listeners can react to:
 * - Update last login timestamp
 * - Send login notification
 * - Log for security audit
 * - Check for suspicious login patterns
 */
@Getter
public class UserLoggedInEvent extends UserEvent {

    private final String ipAddress;
    private final String userAgent;

    public UserLoggedInEvent(Object source, Long userId, String username,
                              String ipAddress, String userAgent) {
        super(source, userId, username, UserEventType.LOGGED_IN);
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    @Override
    public String toString() {
        return String.format("UserLoggedInEvent[userId=%d, username=%s, ip=%s]",
                getUserId(), getUsername(), ipAddress);
    }
}
