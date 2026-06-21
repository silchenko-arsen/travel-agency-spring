package com.example.travelagency.service.email;

public interface EmailService {
    void sendVerificationCode(String to, String code);

    void sendResetToken(String to, String token);
}
