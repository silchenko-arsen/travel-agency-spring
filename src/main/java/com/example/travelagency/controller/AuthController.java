package com.example.travelagency.controller;

import com.example.travelagency.dto.auth.LoginRequest;
import com.example.travelagency.dto.auth.RegisterRequest;
import com.example.travelagency.dto.auth.VerifyEmailRequest;
import com.example.travelagency.exception.BusinessException;
import com.example.travelagency.service.AuthService;
import com.example.travelagency.service.MessageService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MessageService messageService;

    @Value("${app.jwt.cookie-name:JWT_TOKEN}")
    private String jwtCookieName;

    @Value("${app.jwt.expiration-seconds:3600}")
    private long jwtExpirationSeconds;

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("loginRequest") LoginRequest request,
            BindingResult bindingResult,
            Model model,
            HttpServletResponse response
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        try {
            String token = authService.login(request);

            ResponseCookie cookie = ResponseCookie.from(jwtCookieName, token)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(jwtExpirationSeconds)
                    .sameSite("Lax")
                    .build();

            response.addHeader("Set-Cookie", cookie.toString());

            return "redirect:/tours";
        } catch (BusinessException e) {
            model.addAttribute("loginError", messageService.get(e.getCode()));
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.register(request);

            model.addAttribute("verifyEmailRequest", new VerifyEmailRequest());
            model.addAttribute("successMessage", messageService.get("auth.success.register"));

            return "auth/verify";
        } catch (BusinessException e) {
            model.addAttribute("registerError", messageService.get(e.getCode()));
            return "auth/register";
        }
    }

    @GetMapping("/verify")
    public String verifyPage(Model model) {
        model.addAttribute("verifyEmailRequest", new VerifyEmailRequest());
        return "auth/verify";
    }

    @PostMapping("/verify")
    public String verify(
            @Valid @ModelAttribute("verifyEmailRequest") VerifyEmailRequest request,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/verify";
        }

        try {
            authService.verifyEmail(request);
            return "redirect:/auth/login?verified=true";
        } catch (BusinessException e) {
            model.addAttribute("verifyError", messageService.get(e.getCode()));
            return "auth/verify";
        }
    }

    @PostMapping("/resend-code")
    public String resendCode(
            @RequestParam String email,
            Model model
    ) {
        try {
            authService.resendVerificationCode(email);
            model.addAttribute("successMessage", messageService.get("auth.success.codeSent"));
        } catch (BusinessException e) {
            model.addAttribute("verifyError", messageService.get(e.getCode()));
        }

        model.addAttribute("verifyEmailRequest", new VerifyEmailRequest());

        return "auth/verify";
    }

    @GetMapping("/resend-code")
    public String resendCodeGet() {
        return "redirect:/auth/verify";
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(jwtCookieName, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return "redirect:/tours";
    }

    @GetMapping("/logout")
    public String logoutGet() {
        return "redirect:/auth/login";
    }
}