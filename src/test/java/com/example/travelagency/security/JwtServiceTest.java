package com.example.travelagency.security;

import com.example.travelagency.domain.AppUser;
import com.example.travelagency.domain.Role;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET =
            "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationSeconds", 3600L);
    }

    @Test
    void generateToken_shouldGenerateToken() {
        AppUser user = user("user@email.com");

        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
    }

    @Test
    void extractEmail_shouldReturnEmailFromToken() {
        AppUser user = user("user@email.com");

        String token = jwtService.generateToken(user);

        String email = jwtService.extractEmail(token);

        assertThat(email).isEqualTo("user@email.com");
    }

    @Test
    void isTokenValid_whenTokenIsValid_shouldReturnTrue() {
        AppUser user = user("user@email.com");
        String token = jwtService.generateToken(user);

        UserDetails userDetails = User.withUsername("user@email.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        boolean result = jwtService.isTokenValid(token, userDetails);

        assertThat(result).isTrue();
    }

    @Test
    void isTokenValid_whenEmailDoesNotMatch_shouldReturnFalse() {
        AppUser user = user("user@email.com");
        String token = jwtService.generateToken(user);

        UserDetails userDetails = User.withUsername("another@email.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        boolean result = jwtService.isTokenValid(token, userDetails);

        assertThat(result).isFalse();
    }

    @Test
    void isTokenValid_whenTokenIsExpired_shouldReturnFalse() {
        ReflectionTestUtils.setField(jwtService, "expirationSeconds", -3600L);

        AppUser user = user("user@email.com");
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

    @Test
    void extractEmail_whenTokenIsInvalid_shouldThrowJwtException() {
        assertThatThrownBy(() -> jwtService.extractEmail("bad-token"))
                .isInstanceOf(JwtException.class);
    }

    private AppUser user(String email) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setRole(Role.ROLE_USER);
        return user;
    }
}