package com.micromart.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown for business rule violations.
 * <p>
 * Maps to HTTP 400 Bad Request status by default, but can be customized.
 * <p>
 * Use this exception when:
 * - A business rule is violated (e.g., insufficient inventory)
 * - An operation cannot be completed due to business constraints
 * - Invalid state transitions occur
 *
 * Example usage:
 * <pre>
 * if (inventory.getQuantity() < requestedQuantity) {
 *     throw new BusinessException("Insufficient inventory for product: " + productId);
 * }
 * </pre>
 */
public class BusinessException extends BaseException {

    private static final String DEFAULT_ERROR_CODE = "BUSINESS_ERROR";

    /**
     * Constructor with message (defaults to 400 Bad Request).
     *
     * @param message Error message describing the business rule violation
     */
    public BusinessException(String message) {
        super(message, HttpStatus.BAD_REQUEST, DEFAULT_ERROR_CODE);
    }

    /**
     * Constructor with message and custom error code.
     *
     * @param message   Error message
     * @param errorCode Custom error code
     */
    public BusinessException(String message, String errorCode) {
        super(message, HttpStatus.BAD_REQUEST, errorCode);
    }

    /**
     * Constructor with message and custom status.
     *
     * @param message Error message
     * @param status  HTTP status code
     */
    public BusinessException(String message, HttpStatus status) {
        super(message, status, DEFAULT_ERROR_CODE);
    }

    /**
     * Constructor with all parameters.
     *
     * @param message   Error message
     * @param status    HTTP status code
     * @param errorCode Custom error code
     */
    public BusinessException(String message, HttpStatus status, String errorCode) {
        super(message, status, errorCode);
    }

    /**
     * Constructor with cause.
     *
     * @param message Error message
     * @param cause   The underlying cause
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST, DEFAULT_ERROR_CODE);
    }
}
