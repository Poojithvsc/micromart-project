package com.micromart.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration for User Service.
 * <p>
 * Spring Security Annotations:
 * - @EnableWebSecurity: Enables Spring Security
 * - @EnableMethodSecurity: Enables @PreAuthorize, @Secured annotations
 * <p>
 * Configures:
 * - Stateless session management (for JWT)
 * - Public and protected endpoints
 * - CSRF disabled for REST API
 * - Password encoder bean
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

    /**
     * Configure security filter chain.
     * <p>
     * Spring Security: SecurityFilterChain
     * Defines security rules for HTTP requests.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for REST API
                .csrf(csrf -> csrf.disable())
                // Stateless session (JWT-based)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/users/check-email").permitAll()
                        .requestMatchers("/users/check-username").permitAll()
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // All other requests need authentication
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    /**
     * Password encoder bean using BCrypt.
     * <p>
     * BCrypt automatically handles salt generation and storage.
     * Cost factor of 10 (default) provides good security/performance balance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
