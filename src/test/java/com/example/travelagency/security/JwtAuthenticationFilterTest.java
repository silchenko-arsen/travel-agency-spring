package com.example.travelagency.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private UserDetailsService userDetailsService;
    private JwtAuthenticationFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        userDetailsService = mock(UserDetailsService.class);
        filterChain = mock(FilterChain.class);

        filter = new JwtAuthenticationFilter(jwtService, userDetailsService);

        ReflectionTestUtils.setField(filter, "jwtCookieName", "JWT_TOKEN");

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_whenCookiesAreNull_shouldContinueWithoutAuthentication()
            throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenJwtCookieDoesNotExist_shouldContinueWithoutAuthentication()
            throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("OTHER_COOKIE", "jwt-token"));

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenTokenExistsButAuthenticationAlreadyExists_shouldNotAuthenticateAgain()
            throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("JWT_TOKEN", "jwt-token"));

        MockHttpServletResponse response = new MockHttpServletResponse();

        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("existing@email.com", null));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("existing@email.com");

        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenTokenExistsButEmailIsNull_shouldContinueWithoutAuthentication()
            throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("JWT_TOKEN", "jwt-token"));

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractEmail("jwt-token")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(jwtService).extractEmail("jwt-token");
        verifyNoInteractions(userDetailsService);
        verify(jwtService, never()).isTokenValid(anyString(), any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenTokenIsInvalid_shouldContinueWithoutAuthentication()
            throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("JWT_TOKEN", "jwt-token"));

        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDetails userDetails = User.withUsername("user@email.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        when(jwtService.extractEmail("jwt-token")).thenReturn("user@email.com");
        when(userDetailsService.loadUserByUsername("user@email.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("jwt-token", userDetails)).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(jwtService).extractEmail("jwt-token");
        verify(userDetailsService).loadUserByUsername("user@email.com");
        verify(jwtService).isTokenValid("jwt-token", userDetails);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenTokenIsValid_shouldSetAuthentication()
            throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("JWT_TOKEN", "jwt-token"));

        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDetails userDetails = User.withUsername("user@email.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        when(jwtService.extractEmail("jwt-token")).thenReturn("user@email.com");
        when(userDetailsService.loadUserByUsername("user@email.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("jwt-token", userDetails)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("user@email.com");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .hasSize(1);

        verify(jwtService).extractEmail("jwt-token");
        verify(userDetailsService).loadUserByUsername("user@email.com");
        verify(jwtService).isTokenValid("jwt-token", userDetails);
        verify(filterChain).doFilter(request, response);
    }
}