package com.example.appointment.dto.client;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record DoctorAvailabilityResponse(

        Long doctorId,

        LocalDate date,

        boolean working,

        List<LocalTime> slots
) {
}