package com.example.travelagency.controller;

import com.example.travelagency.dto.auth.LoginRequest;
import com.example.travelagency.dto.auth.RegisterRequest;
import com.example.travelagency.exception.BusinessException;
import com.example.travelagency.service.AuthService;
import com.example.travelagency.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
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

        mockMvc = standaloneSetup(controller).build();
    }

    @Test
    void loginPage_shouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeExists("loginRequest"));
    }

    @Test
    void login_whenValid_shouldSetCookieAndRedirectToTours() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn("jwt-token");

        mockMvc.perform(post("/auth/login")
                        .param("email", "user@email.com")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tours"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("JWT_TOKEN=jwt-token")));
    }

    @Test
    void login_whenBusinessException_shouldReturnLoginView() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BusinessException("auth.error.invalidCredentials"));
        when(messageService.get("auth.error.invalidCredentials")).thenReturn("Invalid credentials");

        mockMvc.perform(post("/auth/login")
                        .param("email", "user@email.com")
                        .param("password", "wrong"))
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
    void register_whenValid_shouldRedirectToVerifyWithEmail() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .param("email", "user@email.com")
                        .param("password", "Qwerty1!")
                        .param("firstName", "Arsen")
                        .param("lastName", "Silchenko")
                        .param("phone", "+380501234567"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/verify?email=user%40email.com&registered=true"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void verifyPage_shouldReturnVerifyViewWithEmail() throws Exception {
        mockMvc.perform(get("/auth/verify")
                        .param("email", "user@email.com")
                        .param("registered", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/verify"))
                .andExpect(model().attributeExists("verifyEmailRequest"));
    }

    @Test
    void logout_shouldClearCookieAndRedirectToTours() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tours"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("JWT_TOKEN=;")));
    }
}