package com.example.travelagency.service;

import com.example.travelagency.domain.AppUser;
import com.example.travelagency.domain.Role;
import com.example.travelagency.dto.user.BalanceTopUpRequest;
import com.example.travelagency.dto.user.UserUpdateRequest;
import com.example.travelagency.exception.BusinessException;
import com.example.travelagency.exception.NotFoundException;
import com.example.travelagency.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void getByEmail_whenExists_shouldReturnUser() {
        AppUser user = user();

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));

        AppUser result = userService.getByEmail("user@email.com");

        assertThat(result).isEqualTo(user);
    }

    @Test
    void getByEmail_whenNotExists_shouldThrowNotFoundException() {
        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByEmail("user@email.com"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateProfile_shouldUpdateUserFields() {
        AppUser user = user();

        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("New");
        request.setLastName("Name");
        request.setPhone("+380501234567");

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));

        AppUser result = userService.updateProfile("user@email.com", request);

        assertThat(result.getFirstName()).isEqualTo("New");
        assertThat(result.getLastName()).isEqualTo("Name");
        assertThat(result.getPhone()).isEqualTo("+380501234567");
    }

    @Test
    void topUpBalance_shouldAddAmountToBalance() {
        AppUser user = user();
        user.setBalance(new BigDecimal("100"));

        BalanceTopUpRequest request = new BalanceTopUpRequest();
        request.setAmount(new BigDecimal("50"));

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));

        AppUser result = userService.topUpBalance("user@email.com", request);

        assertThat(result.getBalance()).isEqualByComparingTo("150");
    }

    @Test
    void setBlocked_whenUserIsAdmin_shouldThrowBusinessException() {
        AppUser admin = user();
        admin.setRole(Role.ROLE_ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> userService.setBlocked(1L, true))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void changePassword_whenOldPasswordValid_shouldEncodeNewPassword() {
        AppUser user = user();
        user.setPassword("old-encoded");

        when(userRepository.findByEmailIgnoreCase("user@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "old-encoded")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("new-encoded");

        userService.changePassword("user@email.com", "old", "new");

        assertThat(user.getPassword()).isEqualTo("new-encoded");
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_whenOldPasswordInvalid_shouldThrowBusinessException() {
        AppUser user = user();
        user.setPassword("old-encoded");

        when(userRepository.findByEmailIgnoreCase("user@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "old-encoded")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("user@email.com", "wrong", "new"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("profile.error.oldPasswordInvalid");
    }

    private AppUser user() {
        AppUser user = new AppUser();
        user.setEmail("user@email.com");
        user.setFirstName("Old");
        user.setLastName("User");
        user.setRole(Role.ROLE_USER);
        user.setBalance(BigDecimal.ZERO);
        return user;
    }
}