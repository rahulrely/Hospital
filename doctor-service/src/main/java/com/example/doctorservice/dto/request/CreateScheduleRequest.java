package com.example.doctorservice.dto.request;

import jakarta.validation.constraints.*;

        import java.time.DayOfWeek;
import java.time.LocalTime;

public record CreateScheduleRequest(

        @NotNull
        DayOfWeek dayOfWeek,

        @NotNull
        LocalTime startTime,

        @NotNull
        LocalTime endTime,

        @NotNull
        @Min(5)
        Integer slotDurationMinutes
) {
}