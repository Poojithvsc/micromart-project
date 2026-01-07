package com.micromart.gateway.filter;

import com.micromart.gateway.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Global Authentication Filter for JWT Token Validation.
 * <p>
 * Spring Cloud Gateway: GlobalFilter
 * Applies to all routes and validates JWT tokens before forwarding requests.
 * <p>
 * This filter:
 * - Extracts JWT token from Authorization header
 * - Validates token signature and expiration
 * - Adds user information to request headers for downstream services
 * - Skips authentication for public endpoints (login, register, health)
 * <p>
 * Public endpoints (no authentication required):
 * - /api/auth/** (login, register)
 * - /actuator/** (health checks)
 * - /swagger-ui/** (API documentation)
 */
@Component
@Slf4j
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;

    /**
     * List of paths that don't require authentication.
     */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/",
            "/actuator/",
            "/swagger-ui",
            "/v3/api-docs",
            "/webjars/",
            "/fallback/"
    );

    public AuthenticationFilter(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        this.secretKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Skip authentication for public endpoints
        if (isPublicPath(path)) {
            log.debug("Skipping authentication for public path: {}", path);
            return chain.filter(exchange);
        }

        // Check for Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        // Extract and validate token
        String token = authHeader.substring(7);
        try {
            Claims claims = validateToken(token);

            // Add user information to request headers for downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", claims.getSubject())
                    .header("X-User-Email", claims.get("email", String.class))
                    .header("X-User-Roles", claims.get("roles", String.class))
                    .build();

            log.debug("Authenticated user {} for path: {}", claims.getSubject(), path);

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired for path: {}", path);
            return onError(exchange, "Token has expired", HttpStatus.UNAUTHORIZED);
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token for path: {}", path);
            return onError(exchange, "Invalid token format", HttpStatus.UNAUTHORIZED);
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature for path: {}", path);
            return onError(exchange, "Invalid token signature", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("JWT validation error for path: {}", path, e);
            return onError(exchange, "Token validation failed", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Check if the path is public (doesn't require authentication).
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * Validate JWT token and extract claims.
     */
    private Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Return error response.
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format(
                "{\"success\":false,\"message\":\"%s\",\"timestamp\":\"%s\"}",
                message,
                java.time.LocalDateTime.now()
        );

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8)))
        );
    }

    /**
     * Set filter order to run early in the filter chain.
     */
    @Override
    public int getOrder() {
        return -100;
    }
}
