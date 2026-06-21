package com.example.travelagency.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageSource messageSource;

    public String get(String code) {
        return messageSource.getMessage(
                code,
                null,
                code,
                LocaleContextHolder.getLocale()
        );
    }
}