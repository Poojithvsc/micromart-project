package com.micromart.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found.
 * <p>
 * Maps to HTTP 404 Not Found status.
 * <p>
 * Use this exception when:
 * - An entity with the given ID doesn't exist
 * - A resource referenced by another entity is missing
 * - A lookup by unique field returns no results
 *
 * Example usage:
 * <pre>
 * User user = userRepository.findById(id)
 *     .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
 * </pre>
 */
public class ResourceNotFoundException extends BaseException {

    private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";

    /**
     * Constructor with resource details.
     *
     * @param resourceName Name of the resource (e.g., "User", "Product")
     * @param fieldName    The field used for lookup (e.g., "id", "email")
     * @param fieldValue   The value that was not found
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
                String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue),
                HttpStatus.NOT_FOUND,
                ERROR_CODE
        );
    }

    /**
     * Constructor with simple message.
     *
     * @param message Error message
     */
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ERROR_CODE);
    }
}
