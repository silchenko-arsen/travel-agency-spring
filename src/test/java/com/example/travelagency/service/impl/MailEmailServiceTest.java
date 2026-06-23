package com.example.travelagency.service.impl;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MailEmailServiceTest {

    private JavaMailSender mailSender;
    private MimeMessage mimeMessage;
    private MailEmailService mailEmailService;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        mimeMessage = mock(MimeMessage.class);

        mailEmailService = new MailEmailService(mailSender);

        ReflectionTestUtils.setField(
                mailEmailService,
                "fromEmail",
                "noreply@travel-agency.com"
        );

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendVerificationCode_shouldCreateMessageAndSendEmail() throws MessagingException {
        mailEmailService.sendVerificationCode(" user@email.com ", "123456");

        verify(mailSender).createMimeMessage();

        verify(mimeMessage).setFrom(any(Address.class));
        verify(mimeMessage).setRecipient(eq(Message.RecipientType.TO), any(Address.class));
        verify(mimeMessage).setSubject("Travel Agency verification code", "UTF-8");

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendResetToken_shouldCreateMessageAndSendEmail() throws MessagingException {
        mailEmailService.sendResetToken(" user@email.com ", "654321");

        verify(mailSender).createMimeMessage();

        verify(mimeMessage).setFrom(any(Address.class));
        verify(mimeMessage).setRecipient(eq(Message.RecipientType.TO), any(Address.class));
        verify(mimeMessage).setSubject("Travel Agency password reset", "UTF-8");

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendVerificationCode_whenMessagingException_shouldThrowIllegalStateException()
            throws MessagingException {

        doThrow(new MessagingException("boom"))
                .when(mimeMessage)
                .setFrom(any(Address.class));

        assertThatThrownBy(() ->
                mailEmailService.sendVerificationCode("user@email.com", "123456")
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not create email message")
                .hasCauseInstanceOf(MessagingException.class);

        verify(mailSender).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendResetToken_whenMessagingException_shouldThrowIllegalStateException()
            throws MessagingException {

        doThrow(new MessagingException("boom"))
                .when(mimeMessage)
                .setFrom(any(Address.class));

        assertThatThrownBy(() ->
                mailEmailService.sendResetToken("user@email.com", "654321")
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not create email message")
                .hasCauseInstanceOf(MessagingException.class);

        verify(mailSender).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}