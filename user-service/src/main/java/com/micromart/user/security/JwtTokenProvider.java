package com.micromart.user.security;

import com.micromart.user.config.JwtConfig;
import com.micromart.user.domain.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT Token Provider - Handles token generation and validation.
 * <p>
 * Demonstrates:
 * - @Value for property injection (alternative to @ConfigurationProperties)
 * - JWT token operations using jjwt library
 * <p>
 * Spring Annotation: @Value
 * Injects values from application properties directly into fields.
 * Supports SpEL expressions and default values.
 */
@Component
@Slf4j
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;

    /**
     * Application name injected using @Value annotation.
     * Demonstrates property injection with default value.
     */
    @Value("${spring.application.name:user-service}")
    private String applicationName;

    /**
     * Token issuer from properties with SpEL default.
     * Demonstrates @Value with SpEL expression.
     */
    @Value("${jwt.issuer:${spring.application.name:MicroMart}}")
    private String issuer;

    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(jwtConfig.getSecret().getBytes())
        ));
    }

    /**
     * Generate access token for authenticated user.
     *
     * @param authentication Spring Security authentication object
     * @return JWT access token
     */
    public String generateAccessToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateAccessToken(userDetails.getUsername(),
                authentication.getAuthorities().stream()
                        .map(Object::toString)
                        .collect(Collectors.toSet()));
    }

    /**
     * Generate access token for a user.
     *
     * @param username User's username
     * @param roles    User's roles
     * @return JWT access token
     */
    public String generateAccessToken(String username, Set<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());

        return Jwts.builder()
                .subject(username)
                .issuer(issuer)
                .claim("roles", roles)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Generate refresh token for a user.
     *
     * @param username User's username
     * @return JWT refresh token
     */
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getRefreshExpiration());

        return Jwts.builder()
                .subject(username)
                .issuer(issuer)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extract username from token.
     *
     * @param token JWT token
     * @return Username from token subject
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * Get token type (access or refresh).
     *
     * @param token JWT token
     * @return Token type
     */
    public String getTokenType(String token) {
        Claims claims = parseToken(token);
        return claims.get("type", String.class);
    }

    /**
     * Validate token.
     *
     * @param token JWT token to validate
     * @return true if token is valid
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Check if token is expired.
     *
     * @param token JWT token
     * @return true if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException ex) {
            return true;
        }
    }

    /**
     * Get expiration date from token.
     *
     * @param token JWT token
     * @return Expiration date
     */
    public Date getExpirationFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }

    /**
     * Parse and validate token, returning claims.
     *
     * @param token JWT token
     * @return Token claims
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Get the configured token prefix.
     *
     * @return Token prefix (e.g., "Bearer ")
     */
    public String getTokenPrefix() {
        return jwtConfig.getTokenPrefix();
    }

    /**
     * Get the configured header name.
     *
     * @return Header name (e.g., "Authorization")
     */
    public String getHeaderString() {
        return jwtConfig.getHeaderString();
    }
}
