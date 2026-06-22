package com.example.travelagency.controller;

import com.example.travelagency.domain.AppUser;
import com.example.travelagency.dto.user.UserUpdateRequest;
import com.example.travelagency.exception.BusinessException;
import com.example.travelagency.service.MessageService;
import com.example.travelagency.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.view.AbstractView;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ProfileControllerTest {

    private UserService userService;
    private ModelMapper modelMapper;
    private MessageService messageService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        modelMapper = mock(ModelMapper.class);
        messageService = mock(MessageService.class);

        mockMvc = standaloneSetup(
                new ProfileController(userService, modelMapper, messageService)
        ).build();
    }

    @Test
    void profile_shouldReturnProfileView() throws Exception {
        AppUser user = user();

        UserUpdateRequest profileForm = new UserUpdateRequest();
        profileForm.setFirstName("Arsen");
        profileForm.setLastName("Silchenko");
        profileForm.setPhone("+380501234567");

        when(userService.getByEmail("user@email.com")).thenReturn(user);
        when(modelMapper.map(user, UserUpdateRequest.class)).thenReturn(profileForm);

        mockMvc.perform(get("/profile")
                        .principal(new TestingAuthenticationToken("user@email.com", null)))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/profile"))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("profileForm", profileForm))
                .andExpect(model().attributeExists("balanceForm"))
                .andExpect(model().attributeExists("passwordForm"));

        verify(userService).getByEmail("user@email.com");
        verify(modelMapper).map(user, UserUpdateRequest.class);
    }

    @Test
    void updateProfile_whenValid_shouldRedirectWithSuccessMessage() throws Exception {
        AppUser user = user();

        when(userService.getByEmail("user@email.com")).thenReturn(user);
        when(messageService.get("profile.success.updated"))
                .thenReturn("Профіль успішно оновлено");

        mockMvc.perform(post("/profile")
                        .principal(new TestingAuthenticationToken("user@email.com", null))
                        .param("firstName", "New")
                        .param("lastName", "Name")
                        .param("phone", "+380501234567"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("success", "Профіль успішно оновлено"));

        verify(userService).updateProfile(any(String.class), any(UserUpdateRequest.class));
        verify(messageService).get("profile.success.updated");
    }

    @Test
    void updateProfile_whenValidationError_shouldReturnProfileView() throws Exception {
        AppUser user = user();

        when(userService.getByEmail("user@email.com")).thenReturn(user);

        mockMvc.perform(post("/profile")
                        .principal(new TestingAuthenticationToken("user@email.com", null))
                        .param("firstName", "")
                        .param("lastName", "")
                        .param("phone", "bad-phone"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/profile"))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attributeExists("balanceForm"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @Test
    void topUp_whenValid_shouldRedirectWithSuccessMessage() throws Exception {
        AppUser user = user();

        when(userService.getByEmail("user@email.com")).thenReturn(user);
        when(messageService.get("profile.success.balanceUpdated"))
                .thenReturn("Баланс успішно поповнено");

        mockMvc.perform(post("/profile/balance")
                        .principal(new TestingAuthenticationToken("user@email.com", null))
                        .param("amount", "100.50"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("success", "Баланс успішно поповнено"));

        verify(userService).topUpBalance(any(String.class), any());
        verify(messageService).get("profile.success.balanceUpdated");
    }

    @Test
    void topUp_whenValidationError_shouldReturnProfileView() throws Exception {
        AppUser user = user();

        UserUpdateRequest profileForm = new UserUpdateRequest();
        profileForm.setFirstName("Arsen");
        profileForm.setLastName("Silchenko");
        profileForm.setPhone("+380501234567");

        when(userService.getByEmail("user@email.com")).thenReturn(user);
        when(modelMapper.map(user, UserUpdateRequest.class)).thenReturn(profileForm);

        mockMvc.perform(post("/profile/balance")
                        .principal(new TestingAuthenticationToken("user@email.com", null))
                        .param("amount", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/profile"))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("profileForm", profileForm))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @Test
    void changePassword_whenValid_shouldRedirectWithPasswordSuccess() throws Exception {
        AppUser user = user();

        when(userService.getByEmail("user@email.com")).thenReturn(user);
        when(messageService.get("profile.success.passwordChanged"))
                .thenReturn("Пароль успішно змінено");

        mockMvc.perform(post("/profile/password")
                        .principal(new TestingAuthenticationToken("user@email.com", null))
                        .param("oldPassword", "Oldpass1!")
                        .param("newPassword", "Newpass1!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("passwordSuccess", "Пароль успішно змінено"));

        verify(userService).changePassword("user@email.com", "Oldpass1!", "Newpass1!");
        verify(messageService).get("profile.success.passwordChanged");
    }

    @Test
    void changePassword_whenBusinessException_shouldReturnProfileWithPasswordError() throws Exception {
        AppUser user = user();

        UserUpdateRequest profileForm = new UserUpdateRequest();
        profileForm.setFirstName("Arsen");
        profileForm.setLastName("Silchenko");
        profileForm.setPhone("+380501234567");

        when(userService.getByEmail("user@email.com")).thenReturn(user);
        when(modelMapper.map(user, UserUpdateRequest.class)).thenReturn(profileForm);

        doThrow(new BusinessException("profile.error.oldPasswordInvalid"))
                .when(userService)
                .changePassword("user@email.com", "Wrongpass1!", "Newpass1!");

        when(messageService.get("profile.error.oldPasswordInvalid"))
                .thenReturn("Старий пароль неправильний");

        mockMvc.perform(post("/profile/password")
                        .principal(new TestingAuthenticationToken("user@email.com", null))
                        .param("oldPassword", "Wrongpass1!")
                        .param("newPassword", "Newpass1!"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/profile"))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("profileForm", profileForm))
                .andExpect(model().attributeExists("balanceForm"))
                .andExpect(model().attribute("passwordError", "Старий пароль неправильний"));

        verify(messageService).get("profile.error.oldPasswordInvalid");
    }

    @Test
    void changePassword_whenValidationError_shouldReturnProfileView() throws Exception {
        AppUser user = user();

        UserUpdateRequest profileForm = new UserUpdateRequest();
        profileForm.setFirstName("Arsen");
        profileForm.setLastName("Silchenko");
        profileForm.setPhone("+380501234567");

        when(userService.getByEmail("user@email.com")).thenReturn(user);
        when(modelMapper.map(user, UserUpdateRequest.class)).thenReturn(profileForm);

        mockMvc.perform(post("/profile/password")
                        .principal(new TestingAuthenticationToken("user@email.com", null))
                        .param("oldPassword", "")
                        .param("newPassword", "weak"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/profile"))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("profileForm", profileForm))
                .andExpect(model().attributeExists("balanceForm"));
    }

    private AppUser user() {
        AppUser user = new AppUser();
        user.setEmail("user@email.com");
        user.setFirstName("Arsen");
        user.setLastName("Silchenko");
        user.setPhone("+380501234567");
        user.setBalance(new BigDecimal("1000"));
        return user;
    }
}