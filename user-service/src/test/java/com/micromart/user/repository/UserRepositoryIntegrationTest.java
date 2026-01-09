package com.micromart.user.repository;

import com.micromart.user.domain.Role;
import com.micromart.user.domain.User;
import com.micromart.user.domain.valueobject.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration Tests for UserRepository with Testcontainers.
 * <p>
 * Testing Concepts Demonstrated:
 * - @DataJpaTest for repository layer testing
 * - Testcontainers for real PostgreSQL database
 * - @DynamicPropertySource for dynamic configuration
 * - Integration testing with actual database operations
 * <p>
 * Learning Points:
 * - Testcontainers spins up real Docker containers for testing
 * - @DataJpaTest only loads JPA-related components (fast)
 * - @AutoConfigureTestDatabase(replace = NONE) uses our container
 * - @Container manages container lifecycle
 * - @Testcontainers enables Testcontainers support
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("UserRepository Integration Tests")
@EnabledIf("isDockerAvailable")
class UserRepositoryIntegrationTest {

    static boolean isDockerAvailable() {
        try {
            DockerClientFactory.instance().client();
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }


    /**
     * Shared PostgreSQL container for all tests.
     * Using PostgreSQL 15 (same as production).
     */
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    /**
     * Dynamically configure datasource properties.
     * This connects Spring to our Testcontainer.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = User.builder()
                .username("testuser")
                .email(Email.of("test@example.com"))
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+1234567890")
                .enabled(true)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .build();
        testUser.addRole(Role.USER);
    }

    // ========================================================================
    // SAVE TESTS
    // ========================================================================
    @Nested
    @DisplayName("Save Operations")
    class SaveOperationsTests {

        @Test
        @DisplayName("Should save user and generate ID")
        void shouldSaveUser_AndGenerateId() {
            // When
            User saved = userRepository.save(testUser);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getUsername()).isEqualTo("testuser");
            assertThat(saved.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should save user with roles")
        void shouldSaveUserWithRoles() {
            // Given
            testUser.addRole(Role.ADMIN);

            // When
            User saved = userRepository.save(testUser);

            // Then
            User found = userRepository.findById(saved.getId()).orElseThrow();
            assertThat(found.getRoles()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN);
        }

        @Test
        @DisplayName("Should update existing user")
        void shouldUpdateExistingUser() {
            // Given
            User saved = userRepository.save(testUser);

            // When
            saved.setFirstName("Updated");
            User updated = userRepository.save(saved);

            // Then
            assertThat(updated.getFirstName()).isEqualTo("Updated");
            assertThat(updated.getId()).isEqualTo(saved.getId());
        }
    }

    // ========================================================================
    // FIND TESTS
    // ========================================================================
    @Nested
    @DisplayName("Find Operations")
    class FindOperationsTests {

        @Test
        @DisplayName("Should find user by ID")
        void shouldFindUserById() {
            // Given
            User saved = userRepository.save(testUser);

            // When
            Optional<User> found = userRepository.findById(saved.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Should find user by username")
        void shouldFindUserByUsername() {
            // Given
            userRepository.save(testUser);

            // When
            Optional<User> found = userRepository.findByUsername("testuser");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getEmail().getValue()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should find user by email value")
        void shouldFindUserByEmailValue() {
            // Given
            userRepository.save(testUser);

            // When
            Optional<User> found = userRepository.findByEmailValue("test@example.com");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Should return empty when user not found")
        void shouldReturnEmpty_WhenUserNotFound() {
            // When
            Optional<User> found = userRepository.findById(999L);

            // Then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should find all users with pagination")
        void shouldFindAllWithPagination() {
            // Given
            userRepository.save(testUser);

            User user2 = User.builder()
                    .username("user2")
                    .email(Email.of("user2@example.com"))
                    .password("password")
                    .enabled(true)
                    .accountNonLocked(true)
                    .accountNonExpired(true)
                    .credentialsNonExpired(true)
                    .build();
            userRepository.save(user2);

            // When
            Page<User> page = userRepository.findAll(PageRequest.of(0, 10));

            // Then
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getTotalElements()).isEqualTo(2);
        }
    }

    // ========================================================================
    // EXISTS TESTS
    // ========================================================================
    @Nested
    @DisplayName("Exists Checks")
    class ExistsChecksTests {

        @Test
        @DisplayName("existsByUsername should return true when exists")
        void existsByUsername_shouldReturnTrue_WhenExists() {
            // Given
            userRepository.save(testUser);

            // When
            boolean exists = userRepository.existsByUsername("testuser");

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("existsByUsername should return false when not exists")
        void existsByUsername_shouldReturnFalse_WhenNotExists() {
            // When
            boolean exists = userRepository.existsByUsername("nonexistent");

            // Then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("existsByEmailValue should return true when exists")
        void existsByEmailValue_shouldReturnTrue_WhenExists() {
            // Given
            userRepository.save(testUser);

            // When
            boolean exists = userRepository.existsByEmailValue("test@example.com");

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("existsByEmailValue should return false when not exists")
        void existsByEmailValue_shouldReturnFalse_WhenNotExists() {
            // When
            boolean exists = userRepository.existsByEmailValue("nonexistent@example.com");

            // Then
            assertThat(exists).isFalse();
        }
    }

    // ========================================================================
    // DELETE TESTS
    // ========================================================================
    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperationsTests {

        @Test
        @DisplayName("Should delete user by entity")
        void shouldDeleteUserByEntity() {
            // Given
            User saved = userRepository.save(testUser);
            assertThat(userRepository.count()).isEqualTo(1);

            // When
            userRepository.delete(saved);

            // Then
            assertThat(userRepository.count()).isZero();
        }

        @Test
        @DisplayName("Should delete all users")
        void shouldDeleteAllUsers() {
            // Given
            userRepository.save(testUser);

            User user2 = User.builder()
                    .username("user2")
                    .email(Email.of("user2@example.com"))
                    .password("password")
                    .enabled(true)
                    .accountNonLocked(true)
                    .accountNonExpired(true)
                    .credentialsNonExpired(true)
                    .build();
            userRepository.save(user2);
            assertThat(userRepository.count()).isEqualTo(2);

            // When
            userRepository.deleteAll();

            // Then
            assertThat(userRepository.count()).isZero();
        }
    }

    // ========================================================================
    // CUSTOM QUERY TESTS
    // ========================================================================
    @Nested
    @DisplayName("Custom Queries")
    class CustomQueryTests {

        @Test
        @DisplayName("Should count active users")
        void shouldCountActiveUsers() {
            // Given
            userRepository.save(testUser);

            User disabledUser = User.builder()
                    .username("disabled")
                    .email(Email.of("disabled@example.com"))
                    .password("password")
                    .enabled(false)
                    .accountNonLocked(true)
                    .accountNonExpired(true)
                    .credentialsNonExpired(true)
                    .build();
            userRepository.save(disabledUser);

            // When
            long activeCount = userRepository.countActiveUsers();

            // Then
            assertThat(activeCount).isEqualTo(1);
        }
    }

    // ========================================================================
    // CONSTRAINT TESTS
    // ========================================================================
    @Nested
    @DisplayName("Database Constraints")
    class ConstraintTests {

        @Test
        @DisplayName("Should enforce unique username constraint")
        void shouldEnforceUniqueUsername() {
            // Given
            userRepository.save(testUser);

            User duplicate = User.builder()
                    .username("testuser") // Same username
                    .email(Email.of("different@example.com"))
                    .password("password")
                    .enabled(true)
                    .accountNonLocked(true)
                    .accountNonExpired(true)
                    .credentialsNonExpired(true)
                    .build();

            // When/Then
            org.junit.jupiter.api.Assertions.assertThrows(
                    Exception.class,
                    () -> {
                        userRepository.saveAndFlush(duplicate);
                    }
            );
        }
    }
}
