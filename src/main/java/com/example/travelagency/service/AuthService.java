package com.example.travelagency.service;

import com.example.travelagency.dto.auth.LoginRequest;
import com.example.travelagency.dto.auth.RegisterRequest;
import com.example.travelagency.dto.auth.VerifyEmailRequest;

public interface AuthService {

    void register(RegisterRequest request);

    String login(LoginRequest request);

    void verifyEmail(VerifyEmailRequest request);

    void resendVerificationCode(String emailValue);

    void requestPasswordReset(String emailValue);

    void resetPassword(String emailValue, String token, String newPassword);
}