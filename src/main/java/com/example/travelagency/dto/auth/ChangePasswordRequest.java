package com.example.travelagency.dto.auth;

import com.example.travelagency.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank(message = "{validation.password.old.required}")
    private String oldPassword;

    @NotBlank(message = "{validation.password.required}")
    @StrongPassword
    private String newPassword;
}