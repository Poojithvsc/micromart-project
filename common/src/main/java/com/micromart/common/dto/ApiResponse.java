package com.micromart.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard API response wrapper following REST best practices.
 * <p>
 * PEAA Pattern: Data Transfer Object (DTO)
 * An object that carries data between processes to reduce the number of method calls.
 * <p>
 * This DTO provides a consistent response structure across all API endpoints:
 * - success: Boolean indicating operation success
 * - message: Human-readable message
 * - data: The actual response payload (generic)
 * - timestamp: When the response was generated
 * - path: The API path that was called
 *
 * @param <T> The type of data being returned
 * @see <a href="https://martinfowler.com/eaaCatalog/dataTransferObject.html">Data Transfer Object</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Indicates whether the operation was successful.
     */
    private boolean success;

    /**
     * Human-readable message describing the result.
     */
    private String message;

    /**
     * The actual response data payload.
     * Can be any type: single object, list, or null.
     */
    private T data;

    /**
     * Timestamp when the response was generated.
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * The API path that was called (useful for debugging).
     */
    private String path;

    /**
     * Factory method for successful responses with data.
     *
     * @param data    The response data
     * @param message Success message
     * @param <T>     Type of the data
     * @return ApiResponse with success=true
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Factory method for successful responses with data (default message).
     *
     * @param data The response data
     * @param <T>  Type of the data
     * @return ApiResponse with success=true
     */
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Operation successful");
    }

    /**
     * Factory method for successful responses without data.
     *
     * @param message Success message
     * @param <T>     Type parameter (will be Void)
     * @return ApiResponse with success=true and no data
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Factory method for error responses.
     *
     * @param message Error message
     * @param <T>     Type parameter
     * @return ApiResponse with success=false
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Factory method for error responses with path.
     *
     * @param message Error message
     * @param path    The API path where error occurred
     * @param <T>     Type parameter
     * @return ApiResponse with success=false
     */
    public static <T> ApiResponse<T> error(String message, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
