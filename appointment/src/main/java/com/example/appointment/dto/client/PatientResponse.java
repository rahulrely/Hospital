package com.example.appointment.dto.client;

public record PatientResponse(

        Long id,

        String firstName,

        String lastName,

        String email,

        String phone
) {
}