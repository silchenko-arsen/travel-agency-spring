package com.example.travelagency.service;

import com.example.travelagency.service.impl.MessageServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessageServiceImplTest {

    @Test
    void get_shouldReturnMessageFromMessageSource() {
        MessageSource messageSource = mock(MessageSource.class);
        MessageServiceImpl messageService = new MessageServiceImpl(messageSource);

        LocaleContextHolder.setLocale(Locale.forLanguageTag("uk"));

        when(messageSource.getMessage(
                "booking.success.booked",
                null,
                "booking.success.booked",
                Locale.forLanguageTag("uk")
        )).thenReturn("Тур успішно заброньовано");

        String result = messageService.get("booking.success.booked");

        assertThat(result).isEqualTo("Тур успішно заброньовано");

        verify(messageSource).getMessage(
                "booking.success.booked",
                null,
                "booking.success.booked",
                Locale.forLanguageTag("uk")
        );

        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void get_whenMessageNotFound_shouldReturnCodeAsDefaultMessage() {
        MessageSource messageSource = mock(MessageSource.class);
        MessageServiceImpl messageService = new MessageServiceImpl(messageSource);

        LocaleContextHolder.setLocale(Locale.ENGLISH);

        when(messageSource.getMessage(
                "unknown.key",
                null,
                "unknown.key",
                Locale.ENGLISH
        )).thenReturn("unknown.key");

        String result = messageService.get("unknown.key");

        assertThat(result).isEqualTo("unknown.key");

        LocaleContextHolder.resetLocaleContext();
    }
}