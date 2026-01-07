package com.micromart.user.domain;

/**
 * User roles enumeration.
 * <p>
 * Defines the available roles in the system for authorization.
 * Used with Spring Security's @PreAuthorize and @Secured annotations.
 */
public enum Role {

    /**
     * Regular user with basic permissions.
     * Can browse products, place orders, manage own profile.
     */
    USER("ROLE_USER"),

    /**
     * Administrator with full system access.
     * Can manage users, products, orders, and system settings.
     */
    ADMIN("ROLE_ADMIN"),

    /**
     * Moderator with limited admin capabilities.
     * Can manage products and view orders.
     */
    MODERATOR("ROLE_MODERATOR");

    private final String authority;

    Role(String authority) {
        this.authority = authority;
    }

    /**
     * Get the Spring Security authority string.
     *
     * @return Authority string (e.g., "ROLE_USER")
     */
    public String getAuthority() {
        return authority;
    }
}
