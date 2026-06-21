package com.example.travelagency.exception;

public abstract class AppException extends RuntimeException {
    protected AppException(String message) {
        super(message);
    }
}
