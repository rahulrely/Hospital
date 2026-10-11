package com.example.doctorservice.dto.response;

import java.time.LocalDate;

public record LeaveResponse(

        Long id,

        Long doctorId,

        LocalDate startDate,

        LocalDate endDate,

        String reason,

        Boolean approved
) {
}