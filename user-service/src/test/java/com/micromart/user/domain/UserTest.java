package com.micromart.user.domain;

import com.micromart.user.domain.valueobject.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit Tests for User Domain Entity.
 * <p>
 * Testing Concepts Demonstrated:
 * - Testing rich domain models with behavior
 * - Testing state transitions
 * - Testing business rules encapsulated in entities
 * <p>
 * Learning Points:
 * - Domain entities should contain business logic (PEAA Domain Model pattern)
 * - Tests should verify behavior, not just getters/setters
 * - @Nested classes organize related test cases
 */
@DisplayName("User Domain Entity Tests")
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email(Email.of("test@example.com"))
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .enabled(true)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .build();
    }

    // ========================================================================
    // ROLE MANAGEMENT TESTS
    // ========================================================================
    @Nested
    @DisplayName("Role Management")
    class RoleManagementTests {

        @Test
        @DisplayName("Should add role to user")
        void shouldAddRoleToUser() {
            // When
            user.addRole(Role.USER);

            // Then
            assertThat(user.getRoles()).contains(Role.USER);
            assertThat(user.hasRole(Role.USER)).isTrue();
        }

        @Test
        @DisplayName("Should add multiple roles")
        void shouldAddMultipleRoles() {
            // When
            user.addRole(Role.USER);
            user.addRole(Role.ADMIN);

            // Then
            assertThat(user.getRoles()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN);
        }

        @Test
        @DisplayName("Should not duplicate roles")
        void shouldNotDuplicateRoles() {
            // When
            user.addRole(Role.USER);
            user.addRole(Role.USER);

            // Then
            assertThat(user.getRoles()).hasSize(1);
        }

        @Test
        @DisplayName("Should remove role from user")
        void shouldRemoveRoleFromUser() {
            // Given
            user.addRole(Role.USER);
            user.addRole(Role.ADMIN);

            // When
            user.removeRole(Role.ADMIN);

            // Then
            assertThat(user.getRoles()).containsExactly(Role.USER);
            assertThat(user.hasRole(Role.ADMIN)).isFalse();
        }

        @Test
        @DisplayName("isAdmin should return true when user has ADMIN role")
        void isAdmin_shouldReturnTrue_WhenHasAdminRole() {
            // Given
            user.addRole(Role.ADMIN);

            // Then
            assertThat(user.isAdmin()).isTrue();
        }

        @Test
        @DisplayName("isAdmin should return false when user does not have ADMIN role")
        void isAdmin_shouldReturnFalse_WhenNoAdminRole() {
            // Given
            user.addRole(Role.USER);

            // Then
            assertThat(user.isAdmin()).isFalse();
        }
    }

    // ========================================================================
    // LOGIN ATTEMPT TESTS
    // ========================================================================
    @Nested
    @DisplayName("Login Attempts")
    class LoginAttemptsTests {

        @Test
        @DisplayName("Should record successful login and reset failed attempts")
        void shouldRecordSuccessfulLogin() {
            // Given
            user.recordFailedLogin();
            user.recordFailedLogin();
            assertThat(user.getFailedLoginAttempts()).isEqualTo(2);

            // When
            user.recordSuccessfulLogin();

            // Then
            assertThat(user.getFailedLoginAttempts()).isZero();
            assertThat(user.getLastLoginAt()).isNotNull();
        }

        @Test
        @DisplayName("Should increment failed login attempts")
        void shouldIncrementFailedLoginAttempts() {
            // When
            user.recordFailedLogin();
            user.recordFailedLogin();
            user.recordFailedLogin();

            // Then
            assertThat(user.getFailedLoginAttempts()).isEqualTo(3);
            assertThat(user.isAccountNonLocked()).isTrue(); // Still under 5
        }

        @Test
        @DisplayName("Should lock account after 5 failed attempts")
        void shouldLockAccountAfterFiveFailedAttempts() {
            // When
            for (int i = 0; i < 5; i++) {
                user.recordFailedLogin();
            }

            // Then
            assertThat(user.getFailedLoginAttempts()).isEqualTo(5);
            assertThat(user.isAccountNonLocked()).isFalse();
        }

        @Test
        @DisplayName("Should remain locked after more than 5 failed attempts")
        void shouldRemainLockedAfterMoreFailures() {
            // When
            for (int i = 0; i < 7; i++) {
                user.recordFailedLogin();
            }

            // Then
            assertThat(user.getFailedLoginAttempts()).isEqualTo(7);
            assertThat(user.isAccountNonLocked()).isFalse();
        }
    }

    // ========================================================================
    // ACCOUNT STATUS TESTS
    // ========================================================================
    @Nested
    @DisplayName("Account Status")
    class AccountStatusTests {

        @Test
        @DisplayName("Should disable account")
        void shouldDisableAccount() {
            // When
            user.disable();

            // Then
            assertThat(user.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("Should enable account")
        void shouldEnableAccount() {
            // Given
            user.disable();

            // When
            user.enable();

            // Then
            assertThat(user.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("Should unlock account and reset failed attempts")
        void shouldUnlockAccountAndResetFailedAttempts() {
            // Given
            for (int i = 0; i < 5; i++) {
                user.recordFailedLogin();
            }
            assertThat(user.isAccountNonLocked()).isFalse();

            // When
            user.unlock();

            // Then
            assertThat(user.isAccountNonLocked()).isTrue();
            assertThat(user.getFailedLoginAttempts()).isZero();
        }
    }

    // ========================================================================
    // AUTHENTICATION TESTS
    // ========================================================================
    @Nested
    @DisplayName("Authentication Checks")
    class AuthenticationChecksTests {

        @Test
        @DisplayName("canAuthenticate should return true when all flags are valid")
        void canAuthenticate_shouldReturnTrue_WhenAllFlagsValid() {
            // Given - all flags are true by default in setUp

            // Then
            assertThat(user.canAuthenticate()).isTrue();
        }

        @Test
        @DisplayName("canAuthenticate should return false when disabled")
        void canAuthenticate_shouldReturnFalse_WhenDisabled() {
            // When
            user.disable();

            // Then
            assertThat(user.canAuthenticate()).isFalse();
        }

        @Test
        @DisplayName("canAuthenticate should return false when locked")
        void canAuthenticate_shouldReturnFalse_WhenLocked() {
            // When
            for (int i = 0; i < 5; i++) {
                user.recordFailedLogin();
            }

            // Then
            assertThat(user.canAuthenticate()).isFalse();
        }

        @Test
        @DisplayName("canAuthenticate should return false when expired")
        void canAuthenticate_shouldReturnFalse_WhenExpired() {
            // When
            user.setAccountNonExpired(false);

            // Then
            assertThat(user.canAuthenticate()).isFalse();
        }

        @Test
        @DisplayName("canAuthenticate should return false when credentials expired")
        void canAuthenticate_shouldReturnFalse_WhenCredentialsExpired() {
            // When
            user.setCredentialsNonExpired(false);

            // Then
            assertThat(user.canAuthenticate()).isFalse();
        }
    }

    // ========================================================================
    // FULL NAME TESTS
    // ========================================================================
    @Nested
    @DisplayName("Full Name")
    class FullNameTests {

        @Test
        @DisplayName("Should return full name when both first and last names exist")
        void shouldReturnFullName_WhenBothNamesExist() {
            // Then
            assertThat(user.getFullName()).isEqualTo("Test User");
        }

        @Test
        @DisplayName("Should return first name only when last name is null")
        void shouldReturnFirstNameOnly_WhenLastNameNull() {
            // Given
            user.setLastName(null);

            // Then
            assertThat(user.getFullName()).isEqualTo("Test");
        }

        @Test
        @DisplayName("Should return last name only when first name is null")
        void shouldReturnLastNameOnly_WhenFirstNameNull() {
            // Given
            user.setFirstName(null);

            // Then
            assertThat(user.getFullName()).isEqualTo("User");
        }

        @Test
        @DisplayName("Should return username when both names are null")
        void shouldReturnUsername_WhenBothNamesNull() {
            // Given
            user.setFirstName(null);
            user.setLastName(null);

            // Then
            assertThat(user.getFullName()).isEqualTo("testuser");
        }
    }
}
