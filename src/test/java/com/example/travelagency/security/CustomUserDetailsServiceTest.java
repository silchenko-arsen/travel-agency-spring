package com.example.travelagency.security;

import com.example.travelagency.domain.AppUser;
import com.example.travelagency.domain.Role;
import com.example.travelagency.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_whenUserExists_shouldReturnUserPrincipal() {
        AppUser user = user();

        when(userRepository.findByEmailIgnoreCase("user@email.com"))
                .thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername("user@email.com");

        assertThat(result).isInstanceOf(UserPrincipal.class);
        assertThat(result.getUsername()).isEqualTo("user@email.com");
        assertThat(result.getPassword()).isEqualTo("encoded-password");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.isAccountNonLocked()).isTrue();
        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void loadUserByUsername_whenUserDoesNotExist_shouldThrowUsernameNotFoundException() {
        when(userRepository.findByEmailIgnoreCase("missing@email.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                customUserDetailsService.loadUserByUsername("missing@email.com")
        ).isInstanceOf(UsernameNotFoundException.class);
    }

    private AppUser user() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setEmail("user@email.com");
        user.setPassword("encoded-password");
        user.setRole(Role.ROLE_USER);
        user.setVerified(true);
        user.setBlocked(false);
        return user;
    }
}