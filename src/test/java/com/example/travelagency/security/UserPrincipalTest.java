package com.example.travelagency.security;

import com.example.travelagency.domain.AppUser;
import com.example.travelagency.domain.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserPrincipalTest {

    @Test
    void userPrincipal_shouldReturnUserData() {
        AppUser user = new AppUser();
        user.setId(10L);
        user.setEmail("user@email.com");
        user.setPassword("encoded-password");
        user.setRole(Role.ROLE_MANAGER);
        user.setVerified(true);
        user.setBlocked(false);

        UserPrincipal principal = new UserPrincipal(user);

        assertThat(principal.getId()).isEqualTo(10L);
        assertThat(principal.getUser()).isEqualTo(user);
        assertThat(principal.getUsername()).isEqualTo("user@email.com");
        assertThat(principal.getPassword()).isEqualTo("encoded-password");
        assertThat(principal.isEnabled()).isTrue();
        assertThat(principal.isAccountNonLocked()).isTrue();
        assertThat(principal.isAccountNonExpired()).isTrue();
        assertThat(principal.isCredentialsNonExpired()).isTrue();

        assertThat(principal.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_MANAGER");
    }

    @Test
    void isAccountNonLocked_whenUserBlocked_shouldReturnFalse() {
        AppUser user = new AppUser();
        user.setRole(Role.ROLE_USER);
        user.setBlocked(true);

        UserPrincipal principal = new UserPrincipal(user);

        assertThat(principal.isAccountNonLocked()).isFalse();
    }

    @Test
    void isEnabled_whenUserNotVerified_shouldReturnFalse() {
        AppUser user = new AppUser();
        user.setRole(Role.ROLE_USER);
        user.setVerified(false);

        UserPrincipal principal = new UserPrincipal(user);

        assertThat(principal.isEnabled()).isFalse();
    }
}