package com.example.doctorservice.dto.request;


import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateDoctorRequest(

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @NotBlank
        @Email
        String email,

        @NotBlank
        String phone,

        @NotBlank
        String specialization,

        String qualification,

        @Min(0)
        Integer experienceYears,

        @NotNull
        @DecimalMin("0.0")
        BigDecimal consultationFee
) {
}