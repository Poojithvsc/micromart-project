package com.micromart.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Token Response DTO.
 * <p>
 * PEAA Pattern: Data Transfer Object
 * Returns JWT tokens and user info after successful authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private String username;
    private Set<String> roles;

    public static TokenResponse of(String accessToken, String refreshToken,
                                    long expiresIn, String username, Set<String> roles) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .username(username)
                .roles(roles)
                .build();
    }
}
