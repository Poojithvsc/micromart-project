package com.micromart.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when user doesn't have permission to access a resource.
 * <p>
 * Maps to HTTP 403 Forbidden status.
 * <p>
 * Use this exception when:
 * - User is authenticated but lacks required permissions
 * - Access to a specific resource is denied
 * - Role-based access control fails
 *
 * Example usage:
 * <pre>
 * if (!user.hasRole(Role.ADMIN)) {
 *     throw new ForbiddenException("Admin access required");
 * }
 * </pre>
 */
public class ForbiddenException extends BaseException {

    private static final String ERROR_CODE = "FORBIDDEN";

    /**
     * Constructor with message.
     *
     * @param message Error message
     */
    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, ERROR_CODE);
    }

    /**
     * Default constructor with generic message.
     */
    public ForbiddenException() {
        super("Access denied", HttpStatus.FORBIDDEN, ERROR_CODE);
    }
}
