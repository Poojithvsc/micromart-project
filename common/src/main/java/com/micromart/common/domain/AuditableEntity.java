package com.micromart.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Auditable entity extending BaseEntity with audit fields.
 * <p>
 * PEAA Pattern: Layer Supertype with Audit Trail
 * Provides automatic tracking of entity creation and modification.
 * <p>
 * Uses Spring Data JPA's auditing support:
 * - @CreatedDate: Automatically set on entity creation
 * - @LastModifiedDate: Automatically updated on entity modification
 * - @CreatedBy: Username/ID of creator (requires AuditorAware bean)
 * - @LastModifiedBy: Username/ID of last modifier
 * <p>
 * To enable auditing, add @EnableJpaAuditing to your configuration class
 * and provide an AuditorAware bean for user tracking.
 *
 * @see BaseEntity
 * @see org.springframework.data.jpa.domain.support.AuditingEntityListener
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class AuditableEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * Timestamp when the entity was created.
     * Automatically populated by Spring Data JPA auditing.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the entity was last modified.
     * Automatically updated by Spring Data JPA auditing.
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Username or ID of the user who created this entity.
     * Requires an AuditorAware bean to be configured.
     */
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    /**
     * Username or ID of the user who last modified this entity.
     * Requires an AuditorAware bean to be configured.
     */
    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;
}
