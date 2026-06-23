package com.example.travelagency.security;

import com.example.travelagency.domain.AppUser;
import com.example.travelagency.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET =
            "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationSeconds", 3600L);
    }

    @Test
    void generateToken_shouldCreateTokenAndExtractEmail() {
        AppUser user = user();

        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractEmail(token)).isEqualTo("user@email.com");
    }

    @Test
    void isTokenValid_whenUsernameMatchesAndTokenNotExpired_shouldReturnTrue() {
        AppUser user = user();
        String token = jwtService.generateToken(user);

        UserDetails userDetails = User.withUsername("user@email.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        boolean result = jwtService.isTokenValid(token, userDetails);

        assertThat(result).isTrue();
    }

    @Test
    void isTokenValid_whenUsernameDoesNotMatch_shouldReturnFalse() {
        AppUser user = user();
        String token = jwtService.generateToken(user);

        UserDetails userDetails = User.withUsername("other@email.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        boolean result = jwtService.isTokenValid(token, userDetails);

        assertThat(result).isFalse();
    }

    @Test
    void isTokenValid_whenTokenExpired_shouldReturnFalse() {
        ReflectionTestUtils.setField(jwtService, "expirationSeconds", -1L);

        AppUser user = user();
        String token = jwtService.generateToken(user);

        UserDetails userDetails = User.withUsername("user@email.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        boolean result = jwtService.isTokenValid(token, userDetails);

        assertThat(result).isFalse();
    }

    @Test
    void isTokenValid_whenTokenIsInvalid_shouldReturnFalse() {
        UserDetails userDetails = User.withUsername("user@email.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        boolean result = jwtService.isTokenValid("bad-token", userDetails);

        assertThat(result).isFalse();
    }

    private AppUser user() {
        AppUser user = new AppUser();
        user.setEmail("user@email.com");
        user.setRole(Role.ROLE_USER);
        return user;
    }
}