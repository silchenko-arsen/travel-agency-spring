package com.example.travelagency.service;

import com.example.travelagency.domain.AppUser;
import com.example.travelagency.domain.Role;
import com.example.travelagency.dto.auth.LoginRequest;
import com.example.travelagency.dto.auth.RegisterRequest;
import com.example.travelagency.dto.auth.VerifyEmailRequest;
import com.example.travelagency.exception.BusinessException;
import com.example.travelagency.repository.UserRepository;
import com.example.travelagency.security.JwtService;
import com.example.travelagency.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public void register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("auth.error.emailExists");
        }

        String code = generateVerificationCode();

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(Role.ROLE_USER);
        user.setVerified(false);
        user.setBlocked(false);
        user.setBalance(BigDecimal.ZERO);
        user.setVerificationCode(code);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);

        emailService.sendVerificationCode(email, code);

        log.info("User registered: {}", email);
    }

    @Transactional(readOnly = true)
    public String login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());

        AppUser user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("auth.error.invalidCredentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("auth.error.invalidCredentials");
        }

        if (user.isBlocked()) {
            throw new BusinessException("auth.error.userBlocked");
        }

        if (!user.isVerified()) {
            throw new BusinessException("auth.error.emailNotVerified");
        }

        log.info("User logged in: {}", email);

        return jwtService.generateToken(user);
    }

    public void verifyEmail(VerifyEmailRequest request) {
        String email = normalizeEmail(request.getEmail());

        AppUser user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("auth.error.userNotFound"));

        if (user.isVerified()) {
            return;
        }

        if (user.getVerificationCode() == null ||
                !user.getVerificationCode().equals(request.getCode())) {
            throw new BusinessException("auth.error.invalidCode");
        }

        if (user.getVerificationCodeExpiresAt() == null ||
                user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("auth.error.codeExpired");
        }

        user.setVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);

        userRepository.save(user);

        log.info("Email verified: {}", email);
    }

    public void resendVerificationCode(String emailValue) {
        String email = normalizeEmail(emailValue);

        AppUser user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("auth.error.userNotFound"));

        if (user.isVerified()) {
            throw new BusinessException("auth.error.emailAlreadyVerified");
        }

        String code = generateVerificationCode();

        user.setVerificationCode(code);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);

        emailService.sendVerificationCode(email, code);

        log.info("Verification code resent to {}", email);
    }

    public void requestPasswordReset(String emailValue) {
        String email = normalizeEmail(emailValue);

        AppUser user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("auth.error.userNotFound"));

        String token = generateVerificationCode();

        user.setResetToken(token);
        user.setResetTokenExpiresAt(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);

        emailService.sendResetToken(email, token);

        log.info("Password reset token sent to {}", email);
    }

    public void resetPassword(String emailValue, String token, String newPassword) {
        String email = normalizeEmail(emailValue);

        AppUser user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException("auth.error.userNotFound"));

        if (user.getResetToken() == null || !user.getResetToken().equals(token)) {
            throw new BusinessException("auth.error.invalidResetToken");
        }

        if (user.getResetTokenExpiresAt() == null ||
                user.getResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("auth.error.resetTokenExpired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiresAt(null);

        userRepository.save(user);

        log.info("Password was reset for {}", email);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String generateVerificationCode() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }
}