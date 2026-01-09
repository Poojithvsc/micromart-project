package com.micromart.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing Configuration.
 * <p>
 * Moved to a separate configuration class to allow @WebMvcTest tests
 * to work without loading JPA auditing infrastructure.
 * <p>
 * @EnableJpaAuditing enables automatic population of:
 * - @CreatedDate fields
 * - @LastModifiedDate fields
 * - @CreatedBy and @LastModifiedBy fields (if AuditorAware is configured)
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
