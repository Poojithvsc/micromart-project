package com.micromart.user.domain;

import com.micromart.common.domain.AuditableEntity;
import com.micromart.user.domain.valueobject.Email;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User Entity - Rich Domain Model.
 * <p>
 * PEAA Pattern: Domain Model
 * An object model of the domain that incorporates both behavior and data.
 * <p>
 * This entity demonstrates:
 * - Rich domain model with business logic methods
 * - Value Object embedding (@Embedded Email)
 * - Enumerated collection for roles
 * - JPA auditing for timestamps
 * - Soft delete capability
 * <p>
 * JPA Annotations:
 * - @Entity: Marks this class as a JPA entity
 * - @Table: Specifies table name and constraints
 * - @Embedded: Embeds the Email value object
 * - @ElementCollection: Maps the roles collection
 * - @Enumerated: Stores enum as string
 *
 * @see <a href="https://martinfowler.com/eaaCatalog/domainModel.html">Domain Model</a>
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email", unique = true),
        @Index(name = "idx_user_username", columnList = "username", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User extends AuditableEntity {

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Email as embedded Value Object.
     * Demonstrates the Value Object pattern with @Embedded.
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email"))
    private Email email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    /**
     * User roles stored as a collection of enums.
     * Uses @ElementCollection for simple value types.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Column(name = "enabled")
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "account_non_expired")
    @Builder.Default
    private boolean accountNonExpired = true;

    @Column(name = "account_non_locked")
    @Builder.Default
    private boolean accountNonLocked = true;

    @Column(name = "credentials_non_expired")
    @Builder.Default
    private boolean credentialsNonExpired = true;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "failed_login_attempts")
    @Builder.Default
    private int failedLoginAttempts = 0;

    // ========================================================================
    // Domain Behavior Methods (Rich Domain Model)
    // ========================================================================

    /**
     * Add a role to the user.
     * Domain behavior encapsulated in the entity.
     *
     * @param role The role to add
     */
    public void addRole(Role role) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
    }

    /**
     * Remove a role from the user.
     *
     * @param role The role to remove
     */
    public void removeRole(Role role) {
        if (this.roles != null) {
            this.roles.remove(role);
        }
    }

    /**
     * Check if user has a specific role.
     *
     * @param role The role to check
     * @return true if user has the role
     */
    public boolean hasRole(Role role) {
        return this.roles != null && this.roles.contains(role);
    }

    /**
     * Check if user is an admin.
     *
     * @return true if user has ADMIN role
     */
    public boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    /**
     * Record a successful login.
     * Updates last login timestamp and resets failed attempts.
     */
    public void recordSuccessfulLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.failedLoginAttempts = 0;
    }

    /**
     * Record a failed login attempt.
     * Locks account after 5 consecutive failures.
     */
    public void recordFailedLogin() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.accountNonLocked = false;
        }
    }

    /**
     * Unlock the user account.
     */
    public void unlock() {
        this.accountNonLocked = true;
        this.failedLoginAttempts = 0;
    }

    /**
     * Disable the user account.
     */
    public void disable() {
        this.enabled = false;
    }

    /**
     * Enable the user account.
     */
    public void enable() {
        this.enabled = true;
    }

    /**
     * Get full name.
     *
     * @return First name + Last name, or username if names not set
     */
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return username;
    }

    /**
     * Check if account can authenticate.
     *
     * @return true if all account flags are valid
     */
    public boolean canAuthenticate() {
        return enabled && accountNonExpired && accountNonLocked && credentialsNonExpired;
    }
}
