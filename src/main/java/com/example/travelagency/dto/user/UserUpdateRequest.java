package com.example.travelagency.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {
    @NotBlank(message = "{validation.firstName.required}")
    private String firstName;

    @NotBlank(message = "{validation.lastName.required}")
    private String lastName;

    @Pattern(regexp = "^$|^\\+?[1-9]\\d{7,14}$", message = "{validation.phone.invalid}")
    private String phone;
}
