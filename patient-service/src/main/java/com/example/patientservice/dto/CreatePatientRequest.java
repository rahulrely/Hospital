package com.example.patientservice.dto;

import com.example.patientservice.Enum.Gender;
import java.time.LocalDate;

public record CreatePatientRequest(
        Long id,
        String firstName,
        String lastName,
        String email,
        String password,
        LocalDate dateOfBirth,
        Integer age,
        Gender gender,
        String bloodGroup,
        String phone,
        String address
) {}