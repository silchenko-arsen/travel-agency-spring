package com.example.travelagency.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class MailEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendVerificationCode(String to, String code) {
        sendEmail(
                to,
                "Travel Agency verification code",
                "Your verification code: " + code
        );
    }

    @Override
    public void sendResetToken(String to, String token) {
        sendEmail(
                to,
                "Travel Agency password reset",
                "Use this token to reset password: " + token
        );
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to.trim());
            helper.setSubject(subject);
            helper.setText(text, false);

            mailSender.send(message);

            log.info("Email sent to {}", to);
        } catch (MessagingException e) {
            throw new IllegalStateException("Could not create email message", e);
        }
    }
}