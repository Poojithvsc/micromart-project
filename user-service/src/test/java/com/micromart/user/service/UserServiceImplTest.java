package com.micromart.user.service;

import com.micromart.common.exception.DuplicateResourceException;
import com.micromart.common.exception.ResourceNotFoundException;
import com.micromart.user.domain.Role;
import com.micromart.user.domain.User;
import com.micromart.user.domain.valueobject.Email;
import com.micromart.user.dto.request.CreateUserRequest;
import com.micromart.user.dto.request.UpdateUserRequest;
import com.micromart.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for UserServiceImpl.
 * <p>
 * Testing Concepts Demonstrated:
 * - JUnit 5 with @Nested for organizing tests
 * - Mockito for mocking dependencies
 * - AssertJ for fluent assertions
 * - BDD-style given/when/then pattern
 * - ArgumentCaptor for verifying saved entities
 * <p>
 * Learning Points:
 * - @ExtendWith(MockitoExtension.class) enables Mockito annotations
 * - @Mock creates mock objects
 * - @InjectMocks creates the SUT with mocks injected
 * - @DisplayName provides readable test names
 * - @Nested groups related tests together
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private CreateUserRequest createUserRequest;
    private User existingUser;

    @BeforeEach
    void setUp() {
        createUserRequest = CreateUserRequest.builder()
                .username("john.doe")
                .email("john.doe@example.com")
                .password("Password123!")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1234567890")
                .build();

        existingUser = User.builder()
                .id(1L)
                .username("john.doe")
                .email(Email.of("john.doe@example.com"))
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1234567890")
                .enabled(true)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .build();
        existingUser.addRole(Role.USER);
    }

    // ========================================================================
    // CREATE USER TESTS
    // ========================================================================
    @Nested
    @DisplayName("createUser()")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully when username and email are available")
        void shouldCreateUser_WhenUsernameAndEmailAvailable() {
            // Given
            given(userRepository.existsByUsername(anyString())).willReturn(false);
            given(userRepository.existsByEmailValue(anyString())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
            given(userRepository.save(any(User.class))).willAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return user;
            });

            // When
            User result = userService.createUser(createUserRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo("john.doe");
            assertThat(result.getEmail().getValue()).isEqualTo("john.doe@example.com");
            assertThat(result.hasRole(Role.USER)).isTrue();

            // Verify password was encoded
            verify(passwordEncoder).encode("Password123!");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when username already exists")
        void shouldThrowException_WhenUsernameExists() {
            // Given
            given(userRepository.existsByUsername("john.doe")).willReturn(true);

            // When/Then
            assertThatThrownBy(() -> userService.createUser(createUserRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("username");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when email already exists")
        void shouldThrowException_WhenEmailExists() {
            // Given
            given(userRepository.existsByUsername(anyString())).willReturn(false);
            given(userRepository.existsByEmailValue("john.doe@example.com")).willReturn(true);

            // When/Then
            assertThatThrownBy(() -> userService.createUser(createUserRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("email");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should save user with default USER role")
        void shouldSaveUserWithDefaultRole() {
            // Given
            given(userRepository.existsByUsername(anyString())).willReturn(false);
            given(userRepository.existsByEmailValue(anyString())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            given(userRepository.save(userCaptor.capture())).willAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return user;
            });

            // When
            userService.createUser(createUserRequest);

            // Then
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getRoles()).contains(Role.USER);
            assertThat(savedUser.getRoles()).hasSize(1);
        }
    }

    // ========================================================================
    // GET USER TESTS
    // ========================================================================
    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("Should return user when found")
        void shouldReturnUser_WhenFound() {
            // Given
            given(userRepository.findById(1L)).willReturn(Optional.of(existingUser));

            // When
            User result = userService.getById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo("john.doe");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user not found")
        void shouldThrowException_WhenNotFound() {
            // Given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> userService.getById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User")
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindByIdTests {

        @Test
        @DisplayName("Should return Optional with user when found")
        void shouldReturnOptionalWithUser_WhenFound() {
            // Given
            given(userRepository.findById(1L)).willReturn(Optional.of(existingUser));

            // When
            Optional<User> result = userService.findById(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should return empty Optional when not found")
        void shouldReturnEmptyOptional_WhenNotFound() {
            // Given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // When
            Optional<User> result = userService.findById(999L);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByEmail()")
    class FindByEmailTests {

        @Test
        @DisplayName("Should return user when email found")
        void shouldReturnUser_WhenEmailFound() {
            // Given
            given(userRepository.findByEmailValue("john.doe@example.com"))
                    .willReturn(Optional.of(existingUser));

            // When
            Optional<User> result = userService.findByEmail("JOHN.DOE@example.com");

            // Then
            assertThat(result).isPresent();
            verify(userRepository).findByEmailValue("john.doe@example.com");
        }
    }

    @Nested
    @DisplayName("findAll()")
    class FindAllTests {

        @Test
        @DisplayName("Should return paginated users")
        void shouldReturnPaginatedUsers() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(List.of(existingUser), pageable, 1);
            given(userRepository.findAll(pageable)).willReturn(userPage);

            // When
            Page<User> result = userService.findAll(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }
    }

    // ========================================================================
    // UPDATE USER TESTS
    // ========================================================================
    @Nested
    @DisplayName("updateUser()")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user fields when provided")
        void shouldUpdateUserFields_WhenProvided() {
            // Given
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .phoneNumber("+9876543210")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(existingUser));
            given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            User result = userService.updateUser(1L, request);

            // Then
            assertThat(result.getFirstName()).isEqualTo("Jane");
            assertThat(result.getLastName()).isEqualTo("Smith");
            assertThat(result.getPhoneNumber()).isEqualTo("+9876543210");
        }

        @Test
        @DisplayName("Should update email when available")
        void shouldUpdateEmail_WhenAvailable() {
            // Given
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .email("new.email@example.com")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(existingUser));
            given(userRepository.existsByEmailValue("new.email@example.com")).willReturn(false);
            given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            User result = userService.updateUser(1L, request);

            // Then
            assertThat(result.getEmail().getValue()).isEqualTo("new.email@example.com");
        }

        @Test
        @DisplayName("Should throw exception when updating to existing email")
        void shouldThrowException_WhenUpdatingToExistingEmail() {
            // Given
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .email("taken@example.com")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(existingUser));
            given(userRepository.existsByEmailValue("taken@example.com")).willReturn(true);

            // When/Then
            assertThatThrownBy(() -> userService.updateUser(1L, request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("email");
        }

        @Test
        @DisplayName("Should not check email uniqueness when email unchanged")
        void shouldNotCheckEmailUniqueness_WhenEmailUnchanged() {
            // Given
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .email("john.doe@example.com") // Same email
                    .firstName("Updated")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(existingUser));
            given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            userService.updateUser(1L, request);

            // Then
            verify(userRepository, never()).existsByEmailValue(anyString());
        }
    }

    // ========================================================================
    // DELETE USER TESTS
    // ========================================================================
    @Nested
    @DisplayName("deleteUser()")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user when exists")
        void shouldDeleteUser_WhenExists() {
            // Given
            given(userRepository.findById(1L)).willReturn(Optional.of(existingUser));

            // When
            userService.deleteUser(1L);

            // Then
            verify(userRepository).delete(existingUser);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowException_WhenUserNotFound() {
            // Given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> userService.deleteUser(999L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(userRepository, never()).delete(any());
        }
    }

    // ========================================================================
    // AVAILABILITY CHECKS
    // ========================================================================
    @Nested
    @DisplayName("Availability Checks")
    class AvailabilityChecksTests {

        @Test
        @DisplayName("isEmailAvailable should return true when email not taken")
        void isEmailAvailable_shouldReturnTrue_WhenNotTaken() {
            // Given
            given(userRepository.existsByEmailValue("available@example.com")).willReturn(false);

            // When
            boolean result = userService.isEmailAvailable("AVAILABLE@example.com");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("isEmailAvailable should return false when email taken")
        void isEmailAvailable_shouldReturnFalse_WhenTaken() {
            // Given
            given(userRepository.existsByEmailValue("taken@example.com")).willReturn(true);

            // When
            boolean result = userService.isEmailAvailable("taken@example.com");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("isUsernameAvailable should return true when username not taken")
        void isUsernameAvailable_shouldReturnTrue_WhenNotTaken() {
            // Given
            given(userRepository.existsByUsername("newuser")).willReturn(false);

            // When
            boolean result = userService.isUsernameAvailable("newuser");

            // Then
            assertThat(result).isTrue();
        }
    }

    // ========================================================================
    // ACCOUNT STATUS TESTS
    // ========================================================================
    @Nested
    @DisplayName("Account Status Operations")
    class AccountStatusTests {

        @Test
        @DisplayName("enableUser should enable a disabled user")
        void enableUser_shouldEnableUser() {
            // Given
            existingUser.disable();
            given(userRepository.findById(1L)).willReturn(Optional.of(existingUser));
            given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            User result = userService.enableUser(1L);

            // Then
            assertThat(result.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("disableUser should disable an enabled user")
        void disableUser_shouldDisableUser() {
            // Given
            given(userRepository.findById(1L)).willReturn(Optional.of(existingUser));
            given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            User result = userService.disableUser(1L);

            // Then
            assertThat(result.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("unlockUser should unlock a locked user")
        void unlockUser_shouldUnlockUser() {
            // Given
            existingUser.recordFailedLogin();
            existingUser.recordFailedLogin();
            existingUser.recordFailedLogin();
            existingUser.recordFailedLogin();
            existingUser.recordFailedLogin(); // 5 failures = locked

            given(userRepository.findById(1L)).willReturn(Optional.of(existingUser));
            given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

            assertThat(existingUser.isAccountNonLocked()).isFalse();

            // When
            User result = userService.unlockUser(1L);

            // Then
            assertThat(result.isAccountNonLocked()).isTrue();
            assertThat(result.getFailedLoginAttempts()).isZero();
        }
    }
}
