package com.example.doctorservice.dto.response;


import java.time.DayOfWeek;
import java.time.LocalTime;

public record ScheduleResponse(

        Long id,

        Long doctorId,

        DayOfWeek dayOfWeek,

        LocalTime startTime,

        LocalTime endTime,

        Integer slotDurationMinutes,

        Boolean active
) {
}