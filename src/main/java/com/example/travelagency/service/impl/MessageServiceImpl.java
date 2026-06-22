package com.example.travelagency.service.impl;

import com.example.travelagency.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageSource messageSource;

    @Override
    public String get(String code) {
        return messageSource.getMessage(code, null, code, LocaleContextHolder.getLocale());
    }
}