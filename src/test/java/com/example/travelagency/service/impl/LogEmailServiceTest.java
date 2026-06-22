package com.example.travelagency.service.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class LogEmailServiceTest {

    private final LogEmailService logEmailService = new LogEmailService();

    @Test
    void sendVerificationCode_shouldNotThrowException() {
        assertThatCode(() ->
                logEmailService.sendVerificationCode("user@email.com", "123456")
        ).doesNotThrowAnyException();
    }

    @Test
    void sendResetToken_shouldNotThrowException() {
        assertThatCode(() ->
                logEmailService.sendResetToken("user@email.com", "654321")
        ).doesNotThrowAnyException();
    }
}