package com.example.travelagency.service.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(
        name = "app.mail.enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class LogEmailService implements EmailService {

    @Override
    public void sendVerificationCode(String to, String code) {
        log.info("Email sending is disabled. Verification code for {}: {}", to, code);
    }

    @Override
    public void sendResetToken(String to, String token) {
        log.info("Email sending is disabled. Password reset token for {}: {}", to, token);
    }
}