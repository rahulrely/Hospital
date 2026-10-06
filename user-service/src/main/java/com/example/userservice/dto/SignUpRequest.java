package com.example.userservice.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @Email
        @NotBlank
        String email,

        @Size(min = 1, max = 30)
        String name,

        @Size(min = 4)
        String password,

        @Digits(integer = 3, fraction = 0)
        Integer age
) {
}