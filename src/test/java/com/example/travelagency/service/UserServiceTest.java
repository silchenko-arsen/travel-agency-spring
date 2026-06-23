package com.example.travelagency.service;

import com.example.travelagency.domain.AppUser;
import com.example.travelagency.domain.Role;
import com.example.travelagency.dto.user.BalanceTopUpRequest;
import com.example.travelagency.dto.user.UserUpdateRequest;
import com.example.travelagency.exception.BusinessException;
import com.example.travelagency.exception.NotFoundException;
import com.example.travelagency.repository.UserRepository;
import com.example.travelagency.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
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
    private UserServiceImpl userService;

    @Test
    void getByEmail_whenExists_shouldReturnUser() {
        AppUser user = user();

        when(userRepository.findByEmail("user@email.com"))
                .thenReturn(Optional.of(user));

        AppUser result = userService.getByEmail("user@email.com");

        assertThat(result).isEqualTo(user);
    }

    @Test
    void getByEmail_whenNotExists_shouldThrowNotFoundException() {
        when(userRepository.findByEmail("user@email.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByEmail("user@email.com"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void updateProfile_shouldUpdateUserFields() {
        AppUser user = user();

        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("New");
        request.setLastName("Name");
        request.setPhone("+380501234567");

        when(userRepository.findByEmail("user@email.com"))
                .thenReturn(Optional.of(user));

        AppUser result = userService.updateProfile("user@email.com", request);

        assertThat(result.getFirstName()).isEqualTo("New");
        assertThat(result.getLastName()).isEqualTo("Name");
        assertThat(result.getPhone()).isEqualTo("+380501234567");

        verify(userRepository).findByEmail("user@email.com");
    }

    @Test
    void topUpBalance_shouldAddAmountToBalance() {
        AppUser user = user();
        user.setBalance(new BigDecimal("100"));

        BalanceTopUpRequest request = new BalanceTopUpRequest();
        request.setAmount(new BigDecimal("50"));

        when(userRepository.findByEmail("user@email.com"))
                .thenReturn(Optional.of(user));

        AppUser result = userService.topUpBalance("user@email.com", request);

        assertThat(result.getBalance()).isEqualByComparingTo("150");

        verify(userRepository).findByEmail("user@email.com");
    }

    @Test
    void topUpBalance_whenBalanceIsNull_shouldStartFromZero() {
        AppUser user = user();
        user.setBalance(null);

        BalanceTopUpRequest request = new BalanceTopUpRequest();
        request.setAmount(new BigDecimal("50"));

        when(userRepository.findByEmail("user@email.com"))
                .thenReturn(Optional.of(user));

        AppUser result = userService.topUpBalance("user@email.com", request);

        assertThat(result.getBalance()).isEqualByComparingTo("50");
    }

    @Test
    void searchUsers_shouldCallRepository() {
        PageRequest pageable = PageRequest.of(0, 20);

        when(userRepository.search("arsen", pageable))
                .thenReturn(new PageImpl<>(List.of()));

        assertThat(userService.searchUsers("arsen", pageable)).isNotNull();

        verify(userRepository).search("arsen", pageable);
    }

    @Test
    void setBlocked_whenUserIsNotAdmin_shouldChangeBlockedStatus() {
        AppUser user = user();
        user.setRole(Role.ROLE_USER);
        user.setBlocked(false);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        userService.setBlocked(1L, true);

        assertThat(user.isBlocked()).isTrue();

        verify(userRepository).findById(1L);
    }

    @Test
    void setBlocked_whenUserIsAdmin_shouldThrowBusinessException() {
        AppUser admin = user();
        admin.setRole(Role.ROLE_ADMIN);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> userService.setBlocked(1L, true))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Admin cannot be blocked");
    }

    @Test
    void setBlocked_whenUserNotFound_shouldThrowNotFoundException() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.setBlocked(1L, true))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void changePassword_whenOldPasswordValid_shouldEncodeNewPassword() {
        AppUser user = user();
        user.setPassword("old-encoded");

        when(userRepository.findByEmailIgnoreCase("user@email.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("old", "old-encoded"))
                .thenReturn(true);

        when(passwordEncoder.encode("new"))
                .thenReturn("new-encoded");

        userService.changePassword("user@email.com", "old", "new");

        assertThat(user.getPassword()).isEqualTo("new-encoded");

        verify(userRepository).save(user);
    }

    @Test
    void changePassword_whenUserNotFound_shouldThrowBusinessException() {
        when(userRepository.findByEmailIgnoreCase("user@email.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.changePassword("user@email.com", "Oldpass1!", "Newpass1!")
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("auth.error.userNotFound");

        verify(passwordEncoder, never()).matches(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_whenOldPasswordInvalid_shouldThrowBusinessException() {
        AppUser user = user();
        user.setPassword("old-encoded");

        when(userRepository.findByEmailIgnoreCase("user@email.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrong", "old-encoded"))
                .thenReturn(false);

        assertThatThrownBy(() ->
                userService.changePassword("user@email.com", "wrong", "new")
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("profile.error.oldPasswordInvalid");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    private AppUser user() {
        AppUser user = new AppUser();
        user.setEmail("user@email.com");
        user.setFirstName("Old");
        user.setLastName("User");
        user.setPhone("+380501234567");
        user.setRole(Role.ROLE_USER);
        user.setBalance(BigDecimal.ZERO);
        return user;
    }
}