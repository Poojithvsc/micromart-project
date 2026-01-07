package com.micromart.user.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Email Value Object - Immutable email representation.
 * <p>
 * PEAA Pattern: Value Object
 * A small simple object, like money or a date range, whose equality isn't
 * based on identity but on the values of their fields.
 * <p>
 * Characteristics of Value Objects:
 * - Immutable: Once created, cannot be changed
 * - Equality by value: Two emails with same address are equal
 * - Self-validating: Validates email format on creation
 * - Side-effect free: Methods don't change state
 * <p>
 * Spring/JPA Annotation: @Embeddable
 * Allows this class to be embedded in entities rather than having its own table.
 *
 * @see <a href="https://martinfowler.com/eaaCatalog/valueObject.html">Value Object</a>
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Required by JPA
public class Email implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Email validation regex pattern.
     * Validates basic email format: local@domain.tld
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String value;

    /**
     * Private constructor - use factory method.
     *
     * @param value The email address
     */
    private Email(String value) {
        this.value = value.toLowerCase().trim();
    }

    /**
     * Factory method to create a validated Email.
     * <p>
     * Factory Method Pattern: Encapsulates creation logic and validation.
     *
     * @param value The email address string
     * @return Email value object
     * @throws IllegalArgumentException if email format is invalid
     */
    public static Email of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        String trimmed = value.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
        return new Email(trimmed);
    }

    /**
     * Get the domain part of the email.
     *
     * @return Domain (e.g., "example.com" from "user@example.com")
     */
    public String getDomain() {
        return value.substring(value.indexOf('@') + 1);
    }

    /**
     * Get the local part of the email.
     *
     * @return Local part (e.g., "user" from "user@example.com")
     */
    public String getLocalPart() {
        return value.substring(0, value.indexOf('@'));
    }

    @Override
    public String toString() {
        return value;
    }
}
