package com.micromart.user.service;

import com.micromart.user.dto.request.CreateUserRequest;
import com.micromart.user.dto.request.LoginRequest;
import com.micromart.user.dto.request.RefreshTokenRequest;
import com.micromart.user.dto.response.TokenResponse;

/**
 * Authentication Service Interface.
 * <p>
 * PEAA Pattern: Service Layer
 * Defines authentication operations for the application.
 * <p>
 * PEAA Pattern: Separated Interface
 * Interface is separated from implementation for:
 * - Easier testing with mocks
 * - Multiple implementations (e.g., OAuth2)
 * - Clear contract definition
 */
public interface AuthService {

    /**
     * Authenticate user and generate tokens.
     *
     * @param request Login credentials
     * @return Token response with access and refresh tokens
     */
    TokenResponse login(LoginRequest request);

    /**
     * Register a new user and generate tokens.
     *
     * @param request User registration data
     * @return Token response with access and refresh tokens
     */
    TokenResponse register(CreateUserRequest request);

    /**
     * Refresh access token using refresh token.
     *
     * @param request Refresh token request
     * @return New token response with fresh access token
     */
    TokenResponse refreshToken(RefreshTokenRequest request);

    /**
     * Validate an access token.
     *
     * @param token JWT token to validate
     * @return true if token is valid
     */
    boolean validateToken(String token);

    /**
     * Get username from token.
     *
     * @param token JWT token
     * @return Username from token
     */
    String getUsernameFromToken(String token);
}
