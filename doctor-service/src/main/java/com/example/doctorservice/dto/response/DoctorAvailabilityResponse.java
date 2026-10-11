package com.example.doctorservice.dto.response;

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