package com.micromart.user.security;

import com.micromart.user.domain.User;
import com.micromart.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * Custom UserDetailsService implementation.
 * <p>
 * Spring Security: UserDetailsService
 * Core interface that loads user-specific data.
 * Used by Spring Security to authenticate users.
 * <p>
 * This service:
 * - Loads user by username or email
 * - Maps domain roles to Spring Security authorities
 * - Supports account status (enabled, locked, etc.)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by username or email.
     * <p>
     * Spring Security calls this method during authentication.
     *
     * @param usernameOrEmail Username or email address
     * @return UserDetails for Spring Security
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log.debug("Loading user by username or email: {}", usernameOrEmail);

        User user = userRepository.findByUsernameOrEmailValue(usernameOrEmail, usernameOrEmail.toLowerCase())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", usernameOrEmail);
                    return new UsernameNotFoundException("User not found: " + usernameOrEmail);
                });

        return buildUserDetails(user);
    }

    /**
     * Build Spring Security UserDetails from domain User.
     *
     * @param user Domain user entity
     * @return Spring Security UserDetails
     */
    private UserDetails buildUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                user.isAccountNonExpired(),
                user.isCredentialsNonExpired(),
                user.isAccountNonLocked(),
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                        .collect(Collectors.toSet())
        );
    }
}
