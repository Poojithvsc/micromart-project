package com.micromart.user.repository;

import com.micromart.user.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User Repository - Data Access Layer.
 * <p>
 * PEAA Pattern: Repository
 * Mediates between the domain and data mapping layers using a collection-like
 * interface for accessing domain objects.
 * <p>
 * Spring Data JPA provides:
 * - JpaRepository: CRUD operations + pagination + sorting
 * - JpaSpecificationExecutor: Dynamic query support (Specification pattern)
 * - @Query: Custom JPQL/native queries
 * - @EntityGraph: Fetch optimization to avoid N+1 queries
 * <p>
 * Spring Annotation: @Repository
 * Marks this interface as a repository component and enables exception translation.
 *
 * @see <a href="https://martinfowler.com/eaaCatalog/repository.html">Repository Pattern</a>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * Find user by email address.
     * Uses @EntityGraph to eagerly fetch roles (avoiding N+1).
     *
     * @param email The email address to search
     * @return Optional containing the user if found
     */
    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findByEmailValue(String email);

    /**
     * Find user by username.
     *
     * @param username The username to search
     * @return Optional containing the user if found
     */
    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findByUsername(String username);

    /**
     * Find user by username or email.
     * Useful for login where user can provide either.
     *
     * @param username The username
     * @param email    The email address
     * @return Optional containing the user if found
     */
    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findByUsernameOrEmailValue(String username, String email);

    /**
     * Check if email exists.
     *
     * @param email The email to check
     * @return true if email exists
     */
    boolean existsByEmailValue(String email);

    /**
     * Check if username exists.
     *
     * @param username The username to check
     * @return true if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Find all enabled users.
     * Custom JPQL query example.
     *
     * @return List of enabled users
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true")
    List<User> findAllEnabled();

    /**
     * Find users by role.
     * Demonstrates joining collections in JPQL.
     *
     * @param roleName The role name to search
     * @return List of users with the specified role
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") com.micromart.user.domain.Role role);

    /**
     * Count users by role.
     *
     * @param role The role to count
     * @return Number of users with the role
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r = :role")
    long countByRole(@Param("role") com.micromart.user.domain.Role role);

    /**
     * Find locked accounts.
     * Useful for admin monitoring.
     *
     * @return List of locked user accounts
     */
    @Query("SELECT u FROM User u WHERE u.accountNonLocked = false")
    List<User> findLockedAccounts();
}
