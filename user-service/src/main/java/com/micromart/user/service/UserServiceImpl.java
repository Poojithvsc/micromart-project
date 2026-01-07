package com.micromart.user.service;

import com.micromart.common.exception.DuplicateResourceException;
import com.micromart.common.exception.ResourceNotFoundException;
import com.micromart.user.domain.Role;
import com.micromart.user.domain.User;
import com.micromart.user.domain.valueobject.Email;
import com.micromart.user.dto.request.CreateUserRequest;
import com.micromart.user.dto.request.UpdateUserRequest;
import com.micromart.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * User Service Implementation.
 * <p>
 * PEAA Pattern: Service Layer Implementation
 * Coordinates the application's response in each operation.
 * <p>
 * Spring Annotations:
 * - @Service: Marks this class as a service component
 * - @Transactional: Applies transaction management (Unit of Work pattern)
 * - Constructor injection via @RequiredArgsConstructor (Lombok)
 * <p>
 * PEAA Pattern: Unit of Work
 * @Transactional ensures all operations within a method succeed or fail together.
 *
 * @see <a href="https://martinfowler.com/eaaCatalog/unitOfWork.html">Unit of Work</a>
 */
@Service
@Transactional(readOnly = true) // Default to read-only transactions
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional // Override for write operations
    public User createUser(CreateUserRequest request) {
        log.info("Creating new user with username: {}", request.getUsername());

        // Check for duplicates
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }
        if (userRepository.existsByEmailValue(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Create user entity with Value Object
        User user = User.builder()
                .username(request.getUsername())
                .email(Email.of(request.getEmail()))
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .build();

        // Add default role
        user.addRole(Role.USER);

        User savedUser = userRepository.save(user);
        log.info("Created user with ID: {}", savedUser.getId());

        return savedUser;
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailValue(email.toLowerCase());
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public User updateUser(Long id, UpdateUserRequest request) {
        log.info("Updating user with ID: {}", id);

        User user = getById(id);

        // Update fields if provided
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        // Email update requires uniqueness check
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail().getValue())) {
            if (userRepository.existsByEmailValue(request.getEmail())) {
                throw new DuplicateResourceException("User", "email", request.getEmail());
            }
            user.setEmail(Email.of(request.getEmail()));
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        User user = getById(id);
        userRepository.delete(user);
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmailValue(email.toLowerCase());
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Override
    @Transactional
    public User enableUser(Long id) {
        log.info("Enabling user with ID: {}", id);
        User user = getById(id);
        user.enable();
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User disableUser(Long id) {
        log.info("Disabling user with ID: {}", id);
        User user = getById(id);
        user.disable();
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User unlockUser(Long id) {
        log.info("Unlocking user with ID: {}", id);
        User user = getById(id);
        user.unlock();
        return userRepository.save(user);
    }
}
