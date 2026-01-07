package com.micromart.user.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT Configuration Properties.
 * <p>
 * Spring Boot: @ConfigurationProperties
 * Binds external configuration properties to a POJO.
 * Properties are loaded from application.yml under the 'jwt' prefix.
 * <p>
 * Example application.yml:
 * jwt:
 *   secret: mySecretKey
 *   expiration: 86400000
 *   refresh-expiration: 604800000
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtConfig {

    /**
     * Secret key for signing JWT tokens.
     * Should be at least 256 bits (32 characters) for HS256.
     */
    private String secret;

    /**
     * Access token expiration time in milliseconds.
     * Default: 24 hours (86400000 ms)
     */
    private long expiration = 86400000;

    /**
     * Refresh token expiration time in milliseconds.
     * Default: 7 days (604800000 ms)
     */
    private long refreshExpiration = 604800000;

    /**
     * Token prefix used in Authorization header.
     */
    private String tokenPrefix = "Bearer ";

    /**
     * Header name for JWT token.
     */
    private String headerString = "Authorization";
}
