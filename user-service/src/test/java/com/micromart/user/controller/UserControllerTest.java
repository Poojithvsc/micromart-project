package com.micromart.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.micromart.common.exception.DuplicateResourceException;
import com.micromart.common.exception.ResourceNotFoundException;
import com.micromart.user.domain.Role;
import com.micromart.user.domain.User;
import com.micromart.user.domain.valueobject.Email;
import com.micromart.user.dto.request.CreateUserRequest;
import com.micromart.user.dto.request.UpdateUserRequest;
import com.micromart.user.mapper.UserMapper;
import com.micromart.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller Tests for UserController.
 * <p>
 * Testing Concepts Demonstrated:
 * - @WebMvcTest for controller layer testing
 * - MockMvc for HTTP request/response testing
 * - @MockBean for mocking service layer
 * - @WithMockUser for security context
 * - JSON path assertions
 * <p>
 * Learning Points:
 * - @WebMvcTest only loads web layer (fast)
 * - MockMvc simulates HTTP calls without starting server
 * - Security testing with @WithMockUser
 * - Request/Response body testing with ObjectMapper
 */
@WebMvcTest(UserController.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    private User testUser;
    private CreateUserRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
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
        testUser.addRole(Role.USER);

        createRequest = CreateUserRequest.builder()
                .username("john.doe")
                .email("john.doe@example.com")
                .password("Password123!")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1234567890")
                .build();
    }

    // ========================================================================
    // GET USER TESTS
    // ========================================================================
    @Nested
    @DisplayName("GET /api/users/{id}")
    class GetUserByIdTests {

        @Test
        @WithMockUser
        @DisplayName("Should return user when found")
        void shouldReturnUser_WhenFound() throws Exception {
            // Given
            given(userService.getById(1L)).willReturn(testUser);

            // When/Then
            mockMvc.perform(get("/api/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.username", is("john.doe")));
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 404 when user not found")
        void shouldReturn404_WhenUserNotFound() throws Exception {
            // Given
            given(userService.getById(999L))
                    .willThrow(new ResourceNotFoundException("User", "id", 999L));

            // When/Then
            mockMvc.perform(get("/api/users/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401_WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/users/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ========================================================================
    // GET ALL USERS TESTS
    // ========================================================================
    @Nested
    @DisplayName("GET /api/users")
    class GetAllUsersTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return paginated users for admin")
        void shouldReturnPaginatedUsers_ForAdmin() throws Exception {
            // Given
            PageImpl<User> page = new PageImpl<>(
                    List.of(testUser),
                    PageRequest.of(0, 20),
                    1
            );
            given(userService.findAll(any())).willReturn(page);

            // When/Then
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.content", hasSize(1)));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 403 for non-admin user")
        void shouldReturn403_ForNonAdmin() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isForbidden());
        }
    }

    // ========================================================================
    // CREATE USER TESTS
    // ========================================================================
    @Nested
    @DisplayName("POST /api/users")
    class CreateUserTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should create user successfully")
        void shouldCreateUser() throws Exception {
            // Given
            given(userService.createUser(any(CreateUserRequest.class))).willReturn(testUser);

            // When/Then
            mockMvc.perform(post("/api/users")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(header().exists("Location"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 400 for invalid request")
        void shouldReturn400_ForInvalidRequest() throws Exception {
            // Given - invalid request with missing required fields
            CreateUserRequest invalid = CreateUserRequest.builder()
                    .username("")
                    .email("invalid-email")
                    .password("short")
                    .build();

            // When/Then
            mockMvc.perform(post("/api/users")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 409 for duplicate username")
        void shouldReturn409_ForDuplicateUsername() throws Exception {
            // Given
            given(userService.createUser(any(CreateUserRequest.class)))
                    .willThrow(new DuplicateResourceException("User", "username", "john.doe"));

            // When/Then
            mockMvc.perform(post("/api/users")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isConflict());
        }
    }

    // ========================================================================
    // UPDATE USER TESTS
    // ========================================================================
    @Nested
    @DisplayName("PUT /api/users/{id}")
    class UpdateUserTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should update user successfully")
        void shouldUpdateUser() throws Exception {
            // Given
            UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .build();

            User updatedUser = User.builder()
                    .id(1L)
                    .username("john.doe")
                    .email(Email.of("john.doe@example.com"))
                    .password("encodedPassword")
                    .firstName("Jane")
                    .lastName("Smith")
                    .enabled(true)
                    .accountNonLocked(true)
                    .accountNonExpired(true)
                    .credentialsNonExpired(true)
                    .build();

            given(userService.updateUser(eq(1L), any(UpdateUserRequest.class)))
                    .willReturn(updatedUser);

            // When/Then
            mockMvc.perform(put("/api/users/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 404 when updating non-existent user")
        void shouldReturn404_WhenUpdatingNonExistentUser() throws Exception {
            // Given
            UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                    .firstName("Jane")
                    .build();

            given(userService.updateUser(eq(999L), any(UpdateUserRequest.class)))
                    .willThrow(new ResourceNotFoundException("User", "id", 999L));

            // When/Then
            mockMvc.perform(put("/api/users/999")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    // ========================================================================
    // DELETE USER TESTS
    // ========================================================================
    @Nested
    @DisplayName("DELETE /api/users/{id}")
    class DeleteUserTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should delete user successfully")
        void shouldDeleteUser() throws Exception {
            // Given
            doNothing().when(userService).deleteUser(1L);

            // When/Then
            mockMvc.perform(delete("/api/users/1")
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(userService).deleteUser(1L);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 404 when deleting non-existent user")
        void shouldReturn404_WhenDeletingNonExistentUser() throws Exception {
            // Given
            doThrow(new ResourceNotFoundException("User", "id", 999L))
                    .when(userService).deleteUser(999L);

            // When/Then
            mockMvc.perform(delete("/api/users/999")
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 403 for non-admin user")
        void shouldReturn403_ForNonAdmin() throws Exception {
            mockMvc.perform(delete("/api/users/1")
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    // ========================================================================
    // ACCOUNT STATUS TESTS
    // ========================================================================
    @Nested
    @DisplayName("Account Status Endpoints")
    class AccountStatusTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should enable user")
        void shouldEnableUser() throws Exception {
            // Given
            given(userService.enableUser(1L)).willReturn(testUser);

            // When/Then
            mockMvc.perform(patch("/api/users/1/enable")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should disable user")
        void shouldDisableUser() throws Exception {
            // Given
            testUser.disable();
            given(userService.disableUser(1L)).willReturn(testUser);

            // When/Then
            mockMvc.perform(patch("/api/users/1/disable")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should unlock user")
        void shouldUnlockUser() throws Exception {
            // Given
            given(userService.unlockUser(1L)).willReturn(testUser);

            // When/Then
            mockMvc.perform(patch("/api/users/1/unlock")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)));
        }
    }
}
