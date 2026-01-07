package com.micromart.user.controller;

import com.micromart.common.dto.ApiResponse;
import com.micromart.user.dto.request.CreateUserRequest;
import com.micromart.user.dto.request.LoginRequest;
import com.micromart.user.dto.request.RefreshTokenRequest;
import com.micromart.user.dto.response.TokenResponse;
import com.micromart.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller - REST API for auth operations.
 * <p>
 * PEAA Pattern: Remote Facade
 * Provides a coarse-grained facade over the authentication service.
 * <p>
 * Spring Annotations:
 * - @RestController: Combines @Controller and @ResponseBody
 * - @RequestMapping: Maps HTTP requests to handler methods
 * - @PostMapping: Maps POST requests
 * - @RequestBody: Binds request body to method parameter
 * - @Valid: Triggers validation on the request body
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

    private final AuthService authService;

    /**
     * User login endpoint.
     *
     * @param request Login credentials
     * @return Token response with JWT tokens
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and get JWT tokens")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("Login request received for: {}", request.getUsernameOrEmail());
        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    /**
     * User registration endpoint.
     *
     * @param request Registration data
     * @return Token response with JWT tokens
     */
    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register new user and get JWT tokens")
    public ResponseEntity<ApiResponse<TokenResponse>> register(
            @Valid @RequestBody CreateUserRequest request
    ) {
        log.info("Registration request received for: {}", request.getUsername());
        TokenResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Registration successful"));
    }

    /**
     * Refresh token endpoint.
     *
     * @param request Refresh token
     * @return New token response with fresh access token
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        log.debug("Token refresh request received");
        TokenResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    /**
     * Token validation endpoint.
     * Useful for other services to validate tokens.
     *
     * @param token JWT token to validate
     * @return Validation result
     */
    @GetMapping("/validate")
    @Operation(summary = "Validate token", description = "Validate a JWT access token")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(
            @RequestParam String token
    ) {
        boolean isValid = authService.validateToken(token);
        return ResponseEntity.ok(ApiResponse.success(isValid,
                isValid ? "Token is valid" : "Token is invalid"));
    }

    /**
     * Get current user from token.
     *
     * @param token JWT token
     * @return Username from token
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get username from JWT token")
    public ResponseEntity<ApiResponse<String>> getCurrentUser(
            @RequestHeader("Authorization") String token
    ) {
        // Remove "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String username = authService.getUsernameFromToken(token);
        return ResponseEntity.ok(ApiResponse.success(username, "User retrieved successfully"));
    }
}
