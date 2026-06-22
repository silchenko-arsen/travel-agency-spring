package com.example.travelagency.service.impl;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MailEmailServiceTest {

    private JavaMailSender mailSender;
    private MailEmailService mailEmailService;
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        mimeMessage = mock(MimeMessage.class);

        mailEmailService = new MailEmailService(mailSender);

        ReflectionTestUtils.setField(
                mailEmailService,
                "fromEmail",
                "arsenbuter@gmail.com"
        );

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendVerificationCode_shouldSendVerificationEmail() {
        mailEmailService.sendVerificationCode("user@email.com", "123456");

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendResetToken_shouldSendPasswordResetEmail() {
        mailEmailService.sendResetToken("user@email.com", "654321");

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }
}