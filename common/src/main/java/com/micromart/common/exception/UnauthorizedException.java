package com.micromart.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authentication fails or user is not authenticated.
 * <p>
 * Maps to HTTP 401 Unauthorized status.
 * <p>
 * Use this exception when:
 * - Invalid credentials are provided
 * - JWT token is invalid or expired
 * - User is not authenticated
 *
 * Example usage:
 * <pre>
 * if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
 *     throw new UnauthorizedException("Invalid credentials");
 * }
 * </pre>
 */
public class UnauthorizedException extends BaseException {

    private static final String ERROR_CODE = "UNAUTHORIZED";

    /**
     * Constructor with message.
     *
     * @param message Error message
     */
    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, ERROR_CODE);
    }

    /**
     * Default constructor with generic message.
     */
    public UnauthorizedException() {
        super("Authentication required", HttpStatus.UNAUTHORIZED, ERROR_CODE);
    }
}
