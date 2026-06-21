package com.example.travelagency.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    private String email;

    @NotBlank(message = "{validation.password.required}")
    @Size(min = 6, max = 100, message = "{validation.password.size}")
    private String password;

    @NotBlank(message = "{validation.firstName.required}")
    @Size(min = 2, max = 80)
    private String firstName;

    @NotBlank(message = "{validation.lastName.required}")
    @Size(min = 2, max = 80)
    private String lastName;

    @Pattern(
            regexp = "^\\+?[0-9]{10,15}$",
            message = "{validation.phone.invalid}"
    )
    private String phone;
}