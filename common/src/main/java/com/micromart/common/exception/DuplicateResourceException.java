package com.micromart.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when attempting to create a resource that already exists.
 * <p>
 * Maps to HTTP 409 Conflict status.
 * <p>
 * Use this exception when:
 * - Creating an entity with a duplicate unique field (email, username)
 * - Attempting to create a resource that violates uniqueness constraints
 *
 * Example usage:
 * <pre>
 * if (userRepository.existsByEmail(email)) {
 *     throw new DuplicateResourceException("User", "email", email);
 * }
 * </pre>
 */
public class DuplicateResourceException extends BaseException {

    private static final String ERROR_CODE = "DUPLICATE_RESOURCE";

    /**
     * Constructor with resource details.
     *
     * @param resourceName Name of the resource (e.g., "User", "Product")
     * @param fieldName    The field that has a duplicate value (e.g., "email")
     * @param fieldValue   The duplicate value
     */
    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(
                String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue),
                HttpStatus.CONFLICT,
                ERROR_CODE
        );
    }

    /**
     * Constructor with simple message.
     *
     * @param message Error message
     */
    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT, ERROR_CODE);
    }
}
