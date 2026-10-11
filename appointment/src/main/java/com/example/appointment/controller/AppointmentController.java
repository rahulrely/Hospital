package com.example.appointment.controller;

import com.example.appointment.dto.request.CreateAppointmentRequest;
import com.example.appointment.dto.response.AppointmentResponse;
import com.example.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse create(
            @Valid @RequestBody
            CreateAppointmentRequest request
    ) {

        return appointmentService.create(request);
    }

    @GetMapping("/{id}")
    public AppointmentResponse getById(
            @PathVariable Long id
    ) {

        return appointmentService.getById(id);
    }

    @GetMapping
    public Page<AppointmentResponse> getAll(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by("createdAt").descending()
                );

        return appointmentService.getAll(
                pageable
        );
    }

    @GetMapping("/patient/{patientId}")
    public List<AppointmentResponse>
    getPatientAppointments(
            @PathVariable Long patientId
    ) {

        return appointmentService
                .getPatientAppointments(patientId);
    }

    @PostMapping("/{id}/check-in")
    public AppointmentResponse checkIn(
            @PathVariable Long id
    ) {

        return appointmentService.checkIn(id);
    }

    @PostMapping("/{id}/start")
    public AppointmentResponse start(
            @PathVariable Long id
    ) {

        return appointmentService
                .startConsultation(id);
    }

    @PostMapping("/{id}/complete")
    public AppointmentResponse complete(
            @PathVariable Long id
    ) {

        return appointmentService
                .completeConsultation(id);
    }

    @PostMapping("/{id}/cancel")
    public AppointmentResponse cancel(
            @PathVariable Long id
    ) {

        return appointmentService.cancel(id,
                "Cancelled by patient");
    }

    @PostMapping("/{id}/no-show")
    public AppointmentResponse noShow(
            @PathVariable Long id
    ) {

        return appointmentService.noShow(id);
    }
}