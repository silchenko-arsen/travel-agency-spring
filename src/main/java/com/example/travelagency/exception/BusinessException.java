package com.example.travelagency.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String code) {
        super(code);
        this.code = code;
    }
}