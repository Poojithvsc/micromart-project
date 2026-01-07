package com.micromart.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT Configuration Properties.
 * <p>
 * Spring Annotation: @ConfigurationProperties
 * Binds external configuration properties to this class.
 * Properties are prefixed with "jwt" in application.yml.
 * <p>
 * Example configuration:
 * <pre>
 * jwt:
 *   secret: yourSecretKey
 *   expiration: 86400000
 * </pre>
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtConfig {

    /**
     * Secret key used for signing JWT tokens.
     * Should be at least 256 bits for HS256 algorithm.
     */
    private String secret;

    /**
     * Token expiration time in milliseconds.
     * Default: 24 hours (86400000 ms)
     */
    private long expiration = 86400000;
}
