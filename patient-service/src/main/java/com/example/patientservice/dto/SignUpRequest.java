package com.example.patientservice.dto;

import com.example.patientservice.Enum.Gender;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SignUpRequest(
        @Size(min = 1, max = 30)
        String firstName,

        @Size(min = 1, max = 30)
        String lastName,

        @Email
        @NotBlank
        String email,

        @Size(min = 4)
        String password,

        LocalDate dateOfBirth,

        @Digits(integer = 3, fraction = 0)
        Integer age,

        Gender gender,

        String bloodGroup,

        @Size( min = 10, max = 10)
        String phone,

        @Size(min = 5, max = 100)
        String address
) {
}