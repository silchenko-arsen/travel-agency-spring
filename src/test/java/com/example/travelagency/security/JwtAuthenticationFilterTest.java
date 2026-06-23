package com.example.travelagency.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private UserDetailsService userDetailsService;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        userDetailsService = mock(UserDetailsService.class);

        filter = new JwtAuthenticationFilter(jwtService, userDetailsService);

        ReflectionTestUtils.setField(filter, "jwtCookieName", "JWT_TOKEN");
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_whenCookieIsMissing_shouldContinueWithoutAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilter_whenTokenIsValid_shouldSetAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("JWT_TOKEN", "jwt-token"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        UserDetails userDetails = User.withUsername("user@email.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        when(jwtService.extractEmail("jwt-token")).thenReturn("user@email.com");
        when(userDetailsService.loadUserByUsername("user@email.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("jwt-token", userDetails)).thenReturn(true);

        filter.doFilter(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("user@email.com");
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void doFilter_whenTokenIsInvalid_shouldNotSetAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("JWT_TOKEN", "jwt-token"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        UserDetails userDetails = User.withUsername("user@email.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        when(jwtService.extractEmail("jwt-token")).thenReturn("user@email.com");
        when(userDetailsService.loadUserByUsername("user@email.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("jwt-token", userDetails)).thenReturn(false);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilter_whenAuthenticationAlreadyExists_shouldSkipJwtProcessing() throws Exception {
        UserDetails existingUser = User.withUsername("existing@email.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        existingUser,
                        null,
                        existingUser.getAuthorities()
                )
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("JWT_TOKEN", "jwt-token"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("existing@email.com");

        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
    }
}