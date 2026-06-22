package com.example.travelagency.controller;

import com.example.travelagency.domain.AppUser;
import com.example.travelagency.dto.user.BalanceTopUpRequest;
import com.example.travelagency.dto.user.UserUpdateRequest;
import com.example.travelagency.exception.BusinessException;
import com.example.travelagency.service.MessageService;
import com.example.travelagency.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final ModelMapper modelMapper;
    private final MessageService messageService;

    @GetMapping
    public String profile(Authentication authentication, Model model) {
        AppUser user = userService.getByEmail(authentication.getName());

        model.addAttribute("user", user);
        model.addAttribute("profileForm", modelMapper.map(user, UserUpdateRequest.class));
        model.addAttribute("balanceForm", new BalanceTopUpRequest());

        return "profile";
    }

    @PostMapping
    public String updateProfile(
            @Valid @ModelAttribute("profileForm") UserUpdateRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        AppUser user = userService.getByEmail(authentication.getName());

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("balanceForm", new BalanceTopUpRequest());
            return "profile";
        }

        userService.updateProfile(authentication.getName(), request);

        redirectAttributes.addFlashAttribute(
                "success",
                messageService.get("profile.success.updated")
        );

        return "redirect:/profile";
    }

    @PostMapping("/balance")
    public String topUp(
            @Valid @ModelAttribute("balanceForm") BalanceTopUpRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "balanceError",
                    messageService.get("validation.balance.amount.min")
            );
            return "redirect:/profile";
        }

        userService.topUpBalance(authentication.getName(), request);

        redirectAttributes.addFlashAttribute(
                "success",
                messageService.get("profile.success.balanceUpdated")
        );

        return "redirect:/profile";
    }

    @PostMapping("/password")
    public String changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.changePassword(authentication.getName(), oldPassword, newPassword);

            redirectAttributes.addFlashAttribute(
                    "passwordSuccess",
                    messageService.get("profile.success.passwordChanged")
            );
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute(
                    "passwordError",
                    messageService.get(e.getCode())
            );
        }

        return "redirect:/profile";
    }
}