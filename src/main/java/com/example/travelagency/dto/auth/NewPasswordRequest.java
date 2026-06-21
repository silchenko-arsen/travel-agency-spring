package com.example.travelagency.dto.auth;

import com.example.travelagency.validation.StrongPassword;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewPasswordRequest {
    @NotBlank
    private String token;

    @NotBlank(message = "{validation.password.required}")
    @StrongPassword
    private String password;

    @NotBlank(message = "{validation.password.confirm.required}")
    private String confirmPassword;

    @AssertTrue(message = "{validation.password.match}")
    public boolean isPasswordsMatching() {
        return password != null && password.equals(confirmPassword);
    }
}
