package com.micromart.user.service;

import com.micromart.user.domain.User;
import com.micromart.user.dto.request.CreateUserRequest;
import com.micromart.user.dto.request.UpdateUserRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * User Service Interface - Service Layer Abstraction.
 * <p>
 * PEAA Pattern: Service Layer
 * Defines an application's boundary with a layer of services that establishes
 * a set of available operations and coordinates the application's response
 * in each operation.
 * <p>
 * PEAA Pattern: Separated Interface
 * Defines an interface in a separate package from its implementation.
 * This allows for:
 * - Easy mocking in tests
 * - Multiple implementations (e.g., caching decorator)
 * - Clear contract definition
 * <p>
 * Spring concept: Interface-based design for @Service classes
 *
 * @see <a href="https://martinfowler.com/eaaCatalog/serviceLayer.html">Service Layer</a>
 * @see <a href="https://martinfowler.com/eaaCatalog/separatedInterface.html">Separated Interface</a>
 */
public interface UserService {

    /**
     * Create a new user.
     *
     * @param request User creation data
     * @return Created user
     */
    User createUser(CreateUserRequest request);

    /**
     * Find user by ID.
     *
     * @param id User ID
     * @return Optional containing the user if found
     */
    Optional<User> findById(Long id);

    /**
     * Find user by ID or throw exception.
     *
     * @param id User ID
     * @return User
     * @throws com.micromart.common.exception.ResourceNotFoundException if not found
     */
    User getById(Long id);

    /**
     * Find user by email.
     *
     * @param email Email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username.
     *
     * @param username Username
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Get all users with pagination.
     *
     * @param pageable Pagination parameters
     * @return Page of users
     */
    Page<User> findAll(Pageable pageable);

    /**
     * Update user details.
     *
     * @param id      User ID
     * @param request Update data
     * @return Updated user
     */
    User updateUser(Long id, UpdateUserRequest request);

    /**
     * Delete user by ID.
     *
     * @param id User ID
     */
    void deleteUser(Long id);

    /**
     * Check if email is available.
     *
     * @param email Email to check
     * @return true if email is not taken
     */
    boolean isEmailAvailable(String email);

    /**
     * Check if username is available.
     *
     * @param username Username to check
     * @return true if username is not taken
     */
    boolean isUsernameAvailable(String username);

    /**
     * Enable user account.
     *
     * @param id User ID
     * @return Updated user
     */
    User enableUser(Long id);

    /**
     * Disable user account.
     *
     * @param id User ID
     * @return Updated user
     */
    User disableUser(Long id);

    /**
     * Unlock user account.
     *
     * @param id User ID
     * @return Updated user
     */
    User unlockUser(Long id);
}
