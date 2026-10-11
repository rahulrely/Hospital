package com.example.doctorservice.controller;

import com.example.doctorservice.dto.request.CreateScheduleRequest;
import com.example.doctorservice.dto.response.DoctorAvailabilityResponse;
import com.example.doctorservice.dto.response.ScheduleResponse;
import com.example.doctorservice.service.DoctorScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

        import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctors/{doctorId}/schedules")
@RequiredArgsConstructor
public class DoctorScheduleController {

    private final DoctorScheduleService scheduleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleResponse create(
            @PathVariable Long doctorId,
            @Valid @RequestBody CreateScheduleRequest request
    ) {

        return scheduleService.create(
                doctorId,
                request
        );
    }

    @GetMapping
    public List<ScheduleResponse> getSchedules(
            @PathVariable Long doctorId
    ) {

        return scheduleService
                .getDoctorSchedules(doctorId);
    }

    @PutMapping("/{scheduleId}")
    public ScheduleResponse update(
            @PathVariable Long doctorId,
            @PathVariable Long scheduleId,
            @Valid @RequestBody CreateScheduleRequest request
    ) {

        return scheduleService.update(
                doctorId,
                scheduleId,
                request
        );
    }

    @DeleteMapping("/{scheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long doctorId,
            @PathVariable Long scheduleId
    ) {

        scheduleService.delete(
                doctorId,
                scheduleId
        );
    }

    @GetMapping("/availability")
    public DoctorAvailabilityResponse availability(
            @PathVariable Long doctorId,
            @RequestParam LocalDate date
    ) {

        return scheduleService.getAvailability(
                doctorId,
                date
        );
    }
}