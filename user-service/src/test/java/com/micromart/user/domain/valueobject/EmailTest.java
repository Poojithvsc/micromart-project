package com.micromart.user.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit Tests for Email Value Object.
 * <p>
 * Testing Concepts Demonstrated:
 * - Value Object immutability and validation
 * - Parameterized tests with @ParameterizedTest
 * - @ValueSource for multiple test inputs
 * - @NullAndEmptySource for null/empty testing
 * <p>
 * Learning Points:
 * - Value Objects should be immutable
 * - Value Objects should validate their invariants on creation
 * - Value Objects with same values should be equal (value equality)
 */
@DisplayName("Email Value Object Tests")
class EmailTest {

    @Test
    @DisplayName("Should create Email from valid email string")
    void shouldCreateEmail_FromValidString() {
        // When
        Email email = Email.of("test@example.com");

        // Then
        assertThat(email).isNotNull();
        assertThat(email.getValue()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should normalize email to lowercase")
    void shouldNormalizeEmailToLowercase() {
        // When
        Email email = Email.of("TEST@EXAMPLE.COM");

        // Then
        assertThat(email.getValue()).isEqualTo("test@example.com");
    }

    @ParameterizedTest
    @DisplayName("Should throw exception for invalid email formats")
    @ValueSource(strings = {
            "invalid",
            "invalid@",
            "@example.com",
            "invalid@.com",
            "invalid.com",
            "in valid@example.com"
    })
    void shouldThrowException_ForInvalidEmailFormats(String invalidEmail) {
        assertThatThrownBy(() -> Email.of(invalidEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email");
    }

    @ParameterizedTest
    @DisplayName("Should throw exception for null or empty email")
    @NullAndEmptySource
    void shouldThrowException_ForNullOrEmpty(String email) {
        assertThatThrownBy(() -> Email.of(email))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw exception for blank email")
    void shouldThrowException_ForBlankEmail() {
        assertThatThrownBy(() -> Email.of("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @DisplayName("Should accept valid email formats")
    @ValueSource(strings = {
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.com",
            "user@subdomain.example.com",
            "user123@example.co.uk"
    })
    void shouldAcceptValidEmailFormats(String validEmail) {
        // When
        Email email = Email.of(validEmail);

        // Then
        assertThat(email.getValue()).isEqualTo(validEmail.toLowerCase());
    }

    @Test
    @DisplayName("Two Email objects with same value should be equal")
    void shouldBeEqual_WhenSameValue() {
        // Given
        Email email1 = Email.of("test@example.com");
        Email email2 = Email.of("TEST@EXAMPLE.COM");

        // Then
        assertThat(email1).isEqualTo(email2);
        assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
    }

    @Test
    @DisplayName("Two Email objects with different values should not be equal")
    void shouldNotBeEqual_WhenDifferentValues() {
        // Given
        Email email1 = Email.of("test1@example.com");
        Email email2 = Email.of("test2@example.com");

        // Then
        assertThat(email1).isNotEqualTo(email2);
    }

    @Test
    @DisplayName("toString should return the email value")
    void toString_shouldReturnEmailValue() {
        // Given
        Email email = Email.of("test@example.com");

        // Then
        assertThat(email.toString()).contains("test@example.com");
    }
}
