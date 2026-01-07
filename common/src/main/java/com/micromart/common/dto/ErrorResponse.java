package com.micromart.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Error response DTO for consistent error handling across all services.
 * <p>
 * PEAA Pattern: Data Transfer Object (DTO)
 * Provides detailed error information for API consumers.
 * <p>
 * Contains:
 * - status: HTTP status code
 * - error: HTTP status reason phrase
 * - message: Human-readable error message
 * - path: The API path where error occurred
 * - timestamp: When the error occurred
 * - errors: Field-level validation errors (optional)
 * - details: Additional error details (optional)
 *
 * @see <a href="https://martinfowler.com/eaaCatalog/dataTransferObject.html">Data Transfer Object</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * HTTP status code (e.g., 400, 404, 500).
     */
    private int status;

    /**
     * HTTP status reason phrase (e.g., "Bad Request", "Not Found").
     */
    private String error;

    /**
     * Human-readable error message.
     */
    private String message;

    /**
     * The API path where the error occurred.
     */
    private String path;

    /**
     * Timestamp when the error occurred.
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Field-level validation errors.
     * Key: field name, Value: error message
     */
    private Map<String, String> fieldErrors;

    /**
     * List of error details for multiple errors.
     */
    private List<String> details;

    /**
     * Trace ID for distributed tracing (optional).
     */
    private String traceId;

    /**
     * Factory method for simple error response.
     *
     * @param status  HTTP status code
     * @param error   HTTP status reason phrase
     * @param message Error message
     * @param path    API path
     * @return ErrorResponse
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Factory method for validation error response with field errors.
     *
     * @param status      HTTP status code
     * @param message     Error message
     * @param path        API path
     * @param fieldErrors Map of field validation errors
     * @return ErrorResponse
     */
    public static ErrorResponse validationError(int status, String message, String path,
                                                 Map<String, String> fieldErrors) {
        return ErrorResponse.builder()
                .status(status)
                .error("Validation Failed")
                .message(message)
                .path(path)
                .fieldErrors(fieldErrors)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
