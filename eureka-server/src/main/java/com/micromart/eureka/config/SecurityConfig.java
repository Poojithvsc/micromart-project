package com.micromart.eureka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for Eureka Server.
 * <p>
 * Spring Annotation: @Configuration, @EnableWebSecurity
 * Configures Spring Security for the Eureka dashboard and API endpoints.
 * <p>
 * Configuration:
 * - Basic authentication for the Eureka dashboard
 * - CSRF disabled for service registration endpoints
 * - Actuator endpoints accessible for health checks
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configure security filter chain for Eureka Server.
     * <p>
     * Spring Security: SecurityFilterChain
     * Defines the security rules for HTTP requests.
     *
     * @param http HttpSecurity builder
     * @return Configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for Eureka client registrations
                // Eureka clients send POST requests without CSRF tokens
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/eureka/**"))
                // Configure authorization
                .authorizeHttpRequests(auth -> auth
                        // Allow actuator health endpoint without auth
                        .requestMatchers("/actuator/health/**").permitAll()
                        // Allow Eureka client registration without auth
                        .requestMatchers("/eureka/**").permitAll()
                        // All other requests require authentication
                        .anyRequest().authenticated())
                // Use HTTP Basic authentication for dashboard
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
