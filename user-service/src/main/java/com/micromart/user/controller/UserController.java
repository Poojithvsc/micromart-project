package com.micromart.user.controller;

import com.micromart.common.dto.ApiResponse;
import com.micromart.common.dto.PageResponse;
import com.micromart.user.domain.User;
import com.micromart.user.dto.request.CreateUserRequest;
import com.micromart.user.dto.request.UpdateUserRequest;
import com.micromart.user.dto.response.UserResponse;
import com.micromart.user.mapper.UserMapper;
import com.micromart.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * User REST Controller - Remote Facade Pattern.
 * <p>
 * PEAA Pattern: Remote Facade
 * Provides a coarse-grained facade on fine-grained objects to improve
 * efficiency over a network.
 * <p>
 * Spring Annotations:
 * - @RestController: Combines @Controller and @ResponseBody
 * - @RequestMapping: Base path for all endpoints
 * - @GetMapping, @PostMapping, etc.: HTTP method mappings
 * - @Valid: Triggers validation on request body
 * - @RequestBody: Binds request body to DTO
 * - @PathVariable: Extracts path parameters
 * - @PreAuthorize: Method-level security
 *
 * @see <a href="https://martinfowler.com/eaaCatalog/remoteFacade.html">Remote Facade</a>
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * Create a new user.
     * <p>
     * Spring MVC: @PostMapping, @Valid, @RequestBody
     */
    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        log.info("REST request to create user: {}", request.getUsername());
        User user = userService.createUser(request);
        UserResponse response = userMapper.toResponse(user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User created successfully"));
    }

    /**
     * Get user by ID.
     * <p>
     * Spring MVC: @GetMapping, @PathVariable
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        log.info("REST request to get user by ID: {}", id);
        User user = userService.getById(id);
        UserResponse response = userMapper.toResponse(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all users with pagination.
     * <p>
     * Spring Data: @PageableDefault for default pagination
     * Returns PageResponse DTO for consistent pagination info.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("REST request to get all users, page: {}", pageable.getPageNumber());
        Page<User> users = userService.findAll(pageable);
        Page<UserResponse> responsePage = users.map(userMapper::toResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(responsePage)));
    }

    /**
     * Update user.
     * <p>
     * Spring MVC: @PutMapping
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update user details")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("REST request to update user: {}", id);
        User user = userService.updateUser(id, request);
        UserResponse response = userMapper.toResponse(user);
        return ResponseEntity.ok(ApiResponse.success(response, "User updated successfully"));
    }

    /**
     * Delete user.
     * <p>
     * Spring Security: @PreAuthorize for method-level security
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("REST request to delete user: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    /**
     * Check email availability.
     */
    @GetMapping("/check-email")
    @Operation(summary = "Check if email is available")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailAvailability(
            @RequestParam String email) {
        boolean available = userService.isEmailAvailable(email);
        return ResponseEntity.ok(ApiResponse.success(available,
                available ? "Email is available" : "Email is already taken"));
    }

    /**
     * Check username availability.
     */
    @GetMapping("/check-username")
    @Operation(summary = "Check if username is available")
    public ResponseEntity<ApiResponse<Boolean>> checkUsernameAvailability(
            @RequestParam String username) {
        boolean available = userService.isUsernameAvailable(username);
        return ResponseEntity.ok(ApiResponse.success(available,
                available ? "Username is available" : "Username is already taken"));
    }

    /**
     * Enable user account (Admin only).
     */
    @PatchMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Enable user account (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> enableUser(@PathVariable Long id) {
        log.info("REST request to enable user: {}", id);
        User user = userService.enableUser(id);
        return ResponseEntity.ok(ApiResponse.success(userMapper.toResponse(user), "User enabled"));
    }

    /**
     * Disable user account (Admin only).
     */
    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Disable user account (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> disableUser(@PathVariable Long id) {
        log.info("REST request to disable user: {}", id);
        User user = userService.disableUser(id);
        return ResponseEntity.ok(ApiResponse.success(userMapper.toResponse(user), "User disabled"));
    }

    /**
     * Unlock user account (Admin only).
     */
    @PatchMapping("/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unlock user account (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> unlockUser(@PathVariable Long id) {
        log.info("REST request to unlock user: {}", id);
        User user = userService.unlockUser(id);
        return ResponseEntity.ok(ApiResponse.success(userMapper.toResponse(user), "User unlocked"));
    }
}
