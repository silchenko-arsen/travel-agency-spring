package com.example.travelagency.controller;

import com.example.travelagency.exception.BusinessException;
import com.example.travelagency.service.AuthService;
import com.example.travelagency.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AuthControllerTest {

    private AuthService authService;
    private MessageService messageService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        messageService = mock(MessageService.class);

        AuthController controller = new AuthController(authService, messageService);

        ReflectionTestUtils.setField(controller, "jwtCookieName", "JWT_TOKEN");
        ReflectionTestUtils.setField(controller, "jwtExpirationSeconds", 3600L);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = standaloneSetup(controller)
                .setValidator(validator)
                .build();

        SecurityContextHolder.clearContext();
    }

    @Test
    void loginPage_shouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeExists("loginRequest"));
    }

    @Test
    void login_whenValidationErrors_shouldReturnLoginView() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .param("email", "bad-email")
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void login_whenSuccess_shouldSetJwtCookieAndRedirectToTours() throws Exception {
        when(authService.login(any())).thenReturn("jwt-token");

        mockMvc.perform(post("/auth/login")
                        .param("email", "user@email.com")
                        .param("password", "Qwerty1!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tours"))
                .andExpect(header().string("Set-Cookie", containsString("JWT_TOKEN=jwt-token")))
                .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
                .andExpect(header().string("Set-Cookie", containsString("SameSite=Lax")));
    }

    @Test
    void login_whenBusinessException_shouldReturnLoginViewWithError() throws Exception {
        doThrow(new BusinessException("auth.error.invalidCredentials"))
                .when(authService)
                .login(any());

        when(messageService.get("auth.error.invalidCredentials"))
                .thenReturn("Invalid credentials");

        mockMvc.perform(post("/auth/login")
                        .param("email", "user@email.com")
                        .param("password", "Qwerty1!"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute("loginError", "Invalid credentials"));
    }

    @Test
    void registerPage_shouldReturnRegisterView() throws Exception {
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void register_whenValidationErrors_shouldReturnRegisterView() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .param("email", "bad-email")
                        .param("password", "weak")
                        .param("firstName", "")
                        .param("lastName", "")
                        .param("phone", "bad-phone"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));
    }

    @Test
    void register_whenSuccess_shouldRedirectToVerifyWithEmailAndRegisteredParam() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .param("email", "user@email.com")
                        .param("password", "Qwerty1!")
                        .param("firstName", "Arsen")
                        .param("lastName", "Silchenko")
                        .param("phone", "+380501234567"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/verify?email=user%40email.com&registered=true"));

        verify(authService).register(any());
    }

    @Test
    void register_whenBusinessException_shouldReturnRegisterViewWithError() throws Exception {
        doThrow(new BusinessException("auth.error.emailExists"))
                .when(authService)
                .register(any());

        when(messageService.get("auth.error.emailExists"))
                .thenReturn("Email already exists");

        mockMvc.perform(post("/auth/register")
                        .param("email", "user@email.com")
                        .param("password", "Qwerty1!")
                        .param("firstName", "Arsen")
                        .param("lastName", "Silchenko")
                        .param("phone", "+380501234567"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attribute("registerError", "Email already exists"));
    }

    @Test
    void verifyPage_withoutRegisteredParam_shouldReturnVerifyView() throws Exception {
        mockMvc.perform(get("/auth/verify")
                        .param("email", "user@email.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/verify"))
                .andExpect(model().attributeExists("verifyEmailRequest"))
                .andExpect(model().attributeDoesNotExist("successMessage"));
    }

    @Test
    void verifyPage_withRegisteredParam_shouldAddSuccessMessage() throws Exception {
        when(messageService.get("auth.success.register"))
                .thenReturn("Registration successful");

        mockMvc.perform(get("/auth/verify")
                        .param("email", "user@email.com")
                        .param("registered", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/verify"))
                .andExpect(model().attributeExists("verifyEmailRequest"))
                .andExpect(model().attribute("successMessage", "Registration successful"));
    }

    @Test
    void verify_whenValidationErrors_shouldReturnVerifyView() throws Exception {
        mockMvc.perform(post("/auth/verify")
                        .param("email", "bad-email")
                        .param("code", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/verify"));
    }

    @Test
    void verify_whenSuccess_shouldRedirectToLoginWithVerifiedParam() throws Exception {
        mockMvc.perform(post("/auth/verify")
                        .param("email", "user@email.com")
                        .param("code", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login?verified=true"));

        verify(authService).verifyEmail(any());
    }

    @Test
    void verify_whenBusinessException_shouldReturnVerifyViewWithError() throws Exception {
        doThrow(new BusinessException("auth.error.invalidCode"))
                .when(authService)
                .verifyEmail(any());

        when(messageService.get("auth.error.invalidCode"))
                .thenReturn("Invalid code");

        mockMvc.perform(post("/auth/verify")
                        .param("email", "user@email.com")
                        .param("code", "111111"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/verify"))
                .andExpect(model().attribute("verifyError", "Invalid code"));
    }

    @Test
    void resendCode_whenSuccess_shouldReturnVerifyViewWithSuccessMessage() throws Exception {
        when(messageService.get("auth.success.codeSent"))
                .thenReturn("Code sent");

        mockMvc.perform(post("/auth/resend-code")
                        .param("email", "user@email.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/verify"))
                .andExpect(model().attributeExists("verifyEmailRequest"))
                .andExpect(model().attribute("successMessage", "Code sent"));

        verify(authService).resendVerificationCode("user@email.com");
    }

    @Test
    void resendCode_whenBusinessException_shouldReturnVerifyViewWithError() throws Exception {
        doThrow(new BusinessException("auth.error.emailAlreadyVerified"))
                .when(authService)
                .resendVerificationCode("user@email.com");

        when(messageService.get("auth.error.emailAlreadyVerified"))
                .thenReturn("Email already verified");

        mockMvc.perform(post("/auth/resend-code")
                        .param("email", "user@email.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/verify"))
                .andExpect(model().attributeExists("verifyEmailRequest"))
                .andExpect(model().attribute("verifyError", "Email already verified"));
    }

    @Test
    void resendCodeGet_shouldRedirectToVerify() throws Exception {
        mockMvc.perform(get("/auth/resend-code"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/verify"));
    }

    @Test
    void forgotPasswordPage_shouldReturnForgotPasswordView() throws Exception {
        mockMvc.perform(get("/auth/forgot-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/forgot-password"));
    }

    @Test
    void forgotPassword_whenSuccess_shouldRedirectToResetPasswordWithEmailAndSentParam() throws Exception {
        mockMvc.perform(post("/auth/forgot-password")
                        .param("email", "user@email.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/reset-password?email=user%40email.com&sent=true"));

        verify(authService).requestPasswordReset("user@email.com");
    }

    @Test
    void forgotPassword_whenBusinessException_shouldReturnForgotPasswordViewWithError() throws Exception {
        doThrow(new BusinessException("auth.error.userNotFound"))
                .when(authService)
                .requestPasswordReset("missing@email.com");

        when(messageService.get("auth.error.userNotFound"))
                .thenReturn("User not found");

        mockMvc.perform(post("/auth/forgot-password")
                        .param("email", "missing@email.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/forgot-password"))
                .andExpect(model().attribute("resetError", "User not found"));
    }

    @Test
    void resetPasswordPage_withoutSentParam_shouldReturnResetPasswordView() throws Exception {
        mockMvc.perform(get("/auth/reset-password")
                        .param("email", "user@email.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/reset-password"))
                .andExpect(model().attribute("email", "user@email.com"))
                .andExpect(model().attributeDoesNotExist("successMessage"));
    }

    @Test
    void resetPasswordPage_withSentParam_shouldAddSuccessMessage() throws Exception {
        when(messageService.get("auth.success.resetTokenSent"))
                .thenReturn("Reset token sent");

        mockMvc.perform(get("/auth/reset-password")
                        .param("email", "user@email.com")
                        .param("sent", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/reset-password"))
                .andExpect(model().attribute("email", "user@email.com"))
                .andExpect(model().attribute("successMessage", "Reset token sent"));
    }

    @Test
    void resetPassword_whenSuccess_shouldReturnLoginViewWithSuccessMessage() throws Exception {
        when(messageService.get("auth.success.passwordReset"))
                .thenReturn("Password reset successfully");

        mockMvc.perform(post("/auth/reset-password")
                        .param("email", "user@email.com")
                        .param("token", "123456")
                        .param("newPassword", "Newpass1!"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeExists("loginRequest"))
                .andExpect(model().attribute("successMessage", "Password reset successfully"));

        verify(authService).resetPassword("user@email.com", "123456", "Newpass1!");
    }

    @Test
    void resetPassword_whenBusinessException_shouldReturnResetPasswordViewWithError() throws Exception {
        doThrow(new BusinessException("auth.error.invalidResetToken"))
                .when(authService)
                .resetPassword("user@email.com", "000000", "Newpass1!");

        when(messageService.get("auth.error.invalidResetToken"))
                .thenReturn("Invalid token");

        mockMvc.perform(post("/auth/reset-password")
                        .param("email", "user@email.com")
                        .param("token", "000000")
                        .param("newPassword", "Newpass1!"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/reset-password"))
                .andExpect(model().attribute("resetError", "Invalid token"));
    }

    @Test
    void logout_shouldClearCookieAndRedirectToTours() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tours"))
                .andExpect(cookie().maxAge("JWT_TOKEN", 0))
                .andExpect(header().string("Set-Cookie", containsString("JWT_TOKEN=")))
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
    }

    @Test
    void logoutGet_shouldClearCookieAndRedirectToTours() throws Exception {
        mockMvc.perform(get("/auth/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tours"))
                .andExpect(cookie().maxAge("JWT_TOKEN", 0))
                .andExpect(header().string("Set-Cookie", containsString("JWT_TOKEN=")))
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
    }
}