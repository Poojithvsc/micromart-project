package com.micromart.common.exception;

import com.micromart.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for consistent error responses across all services.
 * <p>
 * Spring Annotation: @ControllerAdvice / @RestControllerAdvice
 * Provides centralized exception handling across all @RequestMapping methods.
 * <p>
 * This handler catches:
 * - Custom BaseException and its subclasses
 * - Validation exceptions (MethodArgumentNotValidException)
 * - Generic exceptions as fallback
 * <p>
 * Services can extend this class to add service-specific exception handlers.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle all custom exceptions extending BaseException.
     * Uses the HTTP status defined in the exception.
     *
     * @param ex      The exception
     * @param request The HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(
            BaseException ex,
            HttpServletRequest request) {

        log.error("Business exception: {} - {}", ex.getErrorCode(), ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(error, ex.getStatus());
    }

    /**
     * Handle validation exceptions from @Valid annotations.
     * Returns field-level validation errors.
     *
     * @param ex      The validation exception
     * @param request The HTTP request
     * @return ResponseEntity with ErrorResponse containing field errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed for request {}: {}", request.getRequestURI(), fieldErrors);

        ErrorResponse error = ErrorResponse.validationError(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                request.getRequestURI(),
                fieldErrors
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle IllegalArgumentException.
     *
     * @param ex      The exception
     * @param request The HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn("Illegal argument: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Fallback handler for all unhandled exceptions.
     * Returns a generic 500 Internal Server Error response.
     *
     * @param ex      The exception
     * @param request The HTTP request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unhandled exception at {}: ", request.getRequestURI(), ex);

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
