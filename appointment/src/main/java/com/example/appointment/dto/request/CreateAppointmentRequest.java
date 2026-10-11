package com.example.appointment.dto.request;


import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateAppointmentRequest(

        @NotNull
        Long patientId,

        @NotNull
        Long doctorId,

        @NotNull
        LocalDate appointmentDate,

        @NotNull
        LocalTime appointmentTime,

        String reason
) {
}