package com.micromart.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception class for all custom exceptions in MicroMart.
 * <p>
 * PEAA Pattern: Layer Supertype
 * Acts as the supertype for all custom exceptions, providing:
 * - HTTP status code for proper REST response
 * - Error code for client-side error handling
 * - Consistent exception structure
 * <p>
 * All custom exceptions should extend this class.
 *
 * @see <a href="https://martinfowler.com/eaaCatalog/layerSupertype.html">Layer Supertype</a>
 */
@Getter
public abstract class BaseException extends RuntimeException {

    /**
     * HTTP status code to return in the response.
     */
    private final HttpStatus status;

    /**
     * Machine-readable error code for client-side handling.
     */
    private final String errorCode;

    /**
     * Constructor with message and status.
     *
     * @param message   Human-readable error message
     * @param status    HTTP status to return
     * @param errorCode Machine-readable error code
     */
    protected BaseException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    /**
     * Constructor with message, cause, and status.
     *
     * @param message   Human-readable error message
     * @param cause     The underlying cause
     * @param status    HTTP status to return
     * @param errorCode Machine-readable error code
     */
    protected BaseException(String message, Throwable cause, HttpStatus status, String errorCode) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }
}
