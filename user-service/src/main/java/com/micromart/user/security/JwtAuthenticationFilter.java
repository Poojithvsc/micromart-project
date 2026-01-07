package com.micromart.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter.
 * <p>
 * Spring Security: OncePerRequestFilter
 * Ensures the filter is executed once per request.
 * <p>
 * This filter:
 * - Extracts JWT token from Authorization header
 * - Validates the token
 * - Sets authentication in SecurityContext
 * <p>
 * Spring Annotation: @Component
 * Marks this class as a Spring-managed component.
 * Spring will auto-detect and register this bean.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String jwt = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                String tokenType = jwtTokenProvider.getTokenType(jwt);

                // Only accept access tokens, not refresh tokens
                if (!"refresh".equals(tokenType)) {
                    String username = jwtTokenProvider.getUsernameFromToken(jwt);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Set authentication for user: {}", username);
                }
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from request header.
     *
     * @param request HTTP request
     * @return JWT token without prefix, or null if not found
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtTokenProvider.getHeaderString());

        if (StringUtils.hasText(bearerToken) &&
                bearerToken.startsWith(jwtTokenProvider.getTokenPrefix())) {
            return bearerToken.substring(jwtTokenProvider.getTokenPrefix().length());
        }

        return null;
    }

    /**
     * Skip filter for public endpoints.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth/") ||
                path.startsWith("/actuator/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs");
    }
}
