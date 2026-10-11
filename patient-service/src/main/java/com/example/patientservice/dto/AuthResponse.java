package com.example.patientservice.dto;

public record AuthResponse(
        String token,
        PatientProfileResponse user
) {

}