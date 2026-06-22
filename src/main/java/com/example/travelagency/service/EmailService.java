package com.example.travelagency.service;

public interface EmailService {
    void sendVerificationCode(String to, String code);

    void sendResetToken(String to, String token);
}
