package com.micromart.user.service;

import com.micromart.common.exception.UnauthorizedException;
import com.micromart.user.config.JwtConfig;
import com.micromart.user.domain.User;
import com.micromart.user.dto.request.CreateUserRequest;
import com.micromart.user.dto.request.LoginRequest;
import com.micromart.user.dto.request.RefreshTokenRequest;
import com.micromart.user.dto.response.TokenResponse;
import com.micromart.user.event.UserEventPublisher;
import com.micromart.user.repository.UserRepository;
import com.micromart.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * Authentication Service Implementation.
 * <p>
 * PEAA Pattern: Service Layer Implementation
 * Coordinates authentication operations.
 * <p>
 * Spring Annotations:
 * - @Service: Marks as service component
 * - @Transactional: Transaction management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtConfig jwtConfig;
    private final UserEventPublisher eventPublisher;

    @Override
    @Transactional
    public TokenResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsernameOrEmail());

        try {
            // Authenticate using Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()
                    )
            );

            // Find the user and record successful login
            User user = userRepository.findByUsernameOrEmailValue(
                    request.getUsernameOrEmail(),
                    request.getUsernameOrEmail().toLowerCase()
            ).orElseThrow(() -> new UnauthorizedException("User not found"));

            user.recordSuccessfulLogin();
            userRepository.save(user);

            // Publish login event (for audit and notifications)
            eventPublisher.publishUserLoggedIn(user, "unknown", "unknown");

            // Generate tokens
            return generateTokenResponse(user);

        } catch (BadCredentialsException e) {
            // Record failed login attempt
            userRepository.findByUsernameOrEmailValue(
                    request.getUsernameOrEmail(),
                    request.getUsernameOrEmail().toLowerCase()
            ).ifPresent(user -> {
                user.recordFailedLogin();
                userRepository.save(user);

                // Publish account locked event if account got locked
                if (!user.isAccountNonLocked()) {
                    eventPublisher.publishAccountLocked(user, "Too many failed login attempts");
                }
            });

            log.warn("Invalid credentials for user: {}", request.getUsernameOrEmail());
            throw new UnauthorizedException("Invalid username/email or password");

        } catch (DisabledException e) {
            log.warn("Disabled account login attempt: {}", request.getUsernameOrEmail());
            throw new UnauthorizedException("Account is disabled");

        } catch (LockedException e) {
            log.warn("Locked account login attempt: {}", request.getUsernameOrEmail());
            throw new UnauthorizedException("Account is locked due to too many failed attempts");

        } catch (AuthenticationException e) {
            log.error("Authentication error: {}", e.getMessage());
            throw new UnauthorizedException("Authentication failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public TokenResponse register(CreateUserRequest request) {
        log.info("Registration attempt for user: {}", request.getUsername());

        // Create user using existing UserService
        User user = userService.createUser(request);

        // Publish registration event (triggers welcome email, etc.)
        eventPublisher.publishUserRegistered(user);

        // Generate tokens for the new user
        return generateTokenResponse(user);
    }

    @Override
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        log.debug("Refresh token request");

        String refreshToken = request.getRefreshToken();

        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        // Verify it's a refresh token
        String tokenType = jwtTokenProvider.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new UnauthorizedException("Invalid token type. Expected refresh token");
        }

        // Get username from token
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

        // Find user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        // Verify user can still authenticate
        if (!user.canAuthenticate()) {
            throw new UnauthorizedException("Account is disabled or locked");
        }

        // Generate new access token (keep the same refresh token)
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getUsername(),
                user.getRoles().stream()
                        .map(Enum::name)
                        .collect(Collectors.toSet())
        );

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // Return same refresh token
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpiration())
                .username(user.getUsername())
                .roles(user.getRoles().stream()
                        .map(Enum::name)
                        .collect(Collectors.toSet()))
                .build();
    }

    @Override
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    @Override
    public String getUsernameFromToken(String token) {
        return jwtTokenProvider.getUsernameFromToken(token);
    }

    /**
     * Generate token response for a user.
     *
     * @param user User entity
     * @return Token response with access and refresh tokens
     */
    private TokenResponse generateTokenResponse(User user) {
        var roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        return TokenResponse.of(
                accessToken,
                refreshToken,
                jwtConfig.getExpiration(),
                user.getUsername(),
                roles
        );
    }
}
