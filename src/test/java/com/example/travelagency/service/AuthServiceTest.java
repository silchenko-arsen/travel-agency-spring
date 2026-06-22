package com.example.travelagency.service;

import com.example.travelagency.domain.AppUser;
import com.example.travelagency.domain.Role;
import com.example.travelagency.dto.auth.LoginRequest;
import com.example.travelagency.dto.auth.RegisterRequest;
import com.example.travelagency.dto.auth.VerifyEmailRequest;
import com.example.travelagency.exception.BusinessException;
import com.example.travelagency.repository.UserRepository;
import com.example.travelagency.security.JwtService;
import com.example.travelagency.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_shouldCreateUserAndSendVerificationCode() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("TEST@EMAIL.COM ");
        request.setPassword("Qwerty1!");
        request.setFirstName("Arsen");
        request.setLastName("Silchenko");
        request.setPhone("+380501234567");

        when(userRepository.existsByEmailIgnoreCase("test@email.com")).thenReturn(false);
        when(passwordEncoder.encode("Qwerty1!")).thenReturn("encoded-password");

        authService.register(request);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(captor.capture());

        AppUser savedUser = captor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("test@email.com");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getFirstName()).isEqualTo("Arsen");
        assertThat(savedUser.getLastName()).isEqualTo("Silchenko");
        assertThat(savedUser.getPhone()).isEqualTo("+380501234567");
        assertThat(savedUser.getRole()).isEqualTo(Role.ROLE_USER);
        assertThat(savedUser.isVerified()).isFalse();
        assertThat(savedUser.isBlocked()).isFalse();
        assertThat(savedUser.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(savedUser.getVerificationCode()).hasSize(6);
        assertThat(savedUser.getVerificationCodeExpiresAt()).isAfter(LocalDateTime.now());

        verify(emailService).sendVerificationCode(eq("test@email.com"), any(String.class));
    }

    @Test
    void register_whenEmailExists_shouldThrowBusinessException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@email.com");

        when(userRepository.existsByEmailIgnoreCase("test@email.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("auth.error.emailExists");

        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendVerificationCode(any(), any());
    }

    @Test
    void login_whenCredentialsAreValid_shouldReturnJwtToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("USER@EMAIL.COM ");
        request.setPassword("password");

        AppUser user = new AppUser();
        user.setEmail("user@email.com");
        user.setPassword("encoded");
        user.setVerified(true);
        user.setBlocked(false);

        when(userRepository.findByEmailIgnoreCase("user@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        String token = authService.login(request);

        assertThat(token).isEqualTo("jwt-token");
    }

    @Test
    void login_whenPasswordIsWrong_shouldThrowBusinessException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@email.com");
        request.setPassword("wrong");

        AppUser user = new AppUser();
        user.setPassword("encoded");

        when(userRepository.findByEmailIgnoreCase("user@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("auth.error.invalidCredentials");
    }

    @Test
    void login_whenUserIsBlocked_shouldThrowBusinessException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@email.com");
        request.setPassword("password");

        AppUser user = new AppUser();
        user.setPassword("encoded");
        user.setBlocked(true);
        user.setVerified(true);

        when(userRepository.findByEmailIgnoreCase("user@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("auth.error.userBlocked");
    }

    @Test
    void login_whenEmailIsNotVerified_shouldThrowBusinessException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@email.com");
        request.setPassword("password");

        AppUser user = new AppUser();
        user.setPassword("encoded");
        user.setBlocked(false);
        user.setVerified(false);

        when(userRepository.findByEmailIgnoreCase("user@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("auth.error.emailNotVerified");
    }

    @Test
    void verifyEmail_whenCodeIsValid_shouldVerifyUser() {
        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setEmail("user@email.com");
        request.setCode("123456");

        AppUser user = new AppUser();
        user.setVerified(false);
        user.setVerificationCode("123456");
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(userRepository.findByEmailIgnoreCase("user@email.com")).thenReturn(Optional.of(user));

        authService.verifyEmail(request);

        assertThat(user.isVerified()).isTrue();
        assertThat(user.getVerificationCode()).isNull();
        assertThat(user.getVerificationCodeExpiresAt()).isNull();

        verify(userRepository).save(user);
    }

    @Test
    void verifyEmail_whenCodeExpired_shouldThrowBusinessException() {
        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setEmail("user@email.com");
        request.setCode("123456");

        AppUser user = new AppUser();
        user.setVerificationCode("123456");
        user.setVerificationCodeExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(userRepository.findByEmailIgnoreCase("user@email.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.verifyEmail(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("auth.error.codeExpired");
    }

    @Test
    void resendVerificationCode_whenUserAlreadyVerified_shouldThrowBusinessException() {
        AppUser user = new AppUser();
        user.setVerified(true);

        when(userRepository.findByEmailIgnoreCase("user@email.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.resendVerificationCode("user@email.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("auth.error.emailAlreadyVerified");
    }

    @Test
    void requestPasswordReset_shouldSaveResetTokenAndSendEmail() {
        AppUser user = new AppUser();

        when(userRepository.findByEmailIgnoreCase("user@email.com")).thenReturn(Optional.of(user));

        authService.requestPasswordReset("user@email.com");

        assertThat(user.getResetToken()).hasSize(6);
        assertThat(user.getResetTokenExpiresAt()).isAfter(LocalDateTime.now());

        verify(userRepository).save(user);
        verify(emailService).sendResetToken(eq("user@email.com"), any(String.class));
    }

    @Test
    void resetPassword_whenTokenIsValid_shouldUpdatePassword() {
        AppUser user = new AppUser();
        user.setResetToken("123456");
        user.setResetTokenExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(userRepository.findByEmailIgnoreCase("user@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("Qwerty1!")).thenReturn("encoded-new-password");

        authService.resetPassword("user@email.com", "123456", "Qwerty1!");

        assertThat(user.getPassword()).isEqualTo("encoded-new-password");
        assertThat(user.getResetToken()).isNull();
        assertThat(user.getResetTokenExpiresAt()).isNull();

        verify(userRepository).save(user);
    }
}