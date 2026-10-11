package com.example.doctorservice.service;

import com.example.doctorservice.dto.request.CreateScheduleRequest;
import com.example.doctorservice.dto.response.DoctorAvailabilityResponse;
import com.example.doctorservice.dto.response.ScheduleResponse;

import java.time.LocalDate;
import java.util.List;

public interface DoctorScheduleService {

    ScheduleResponse create(
            Long doctorId,
            CreateScheduleRequest request
    );

    List<ScheduleResponse> getDoctorSchedules(
            Long doctorId
    );

    ScheduleResponse update(
            Long doctorId,
            Long scheduleId,
            CreateScheduleRequest request
    );

    void delete(Long doctorId, Long scheduleId);

    DoctorAvailabilityResponse getAvailability(
            Long doctorId,
            LocalDate date
    );
}