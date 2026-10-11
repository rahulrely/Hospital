package com.example.appointment.client;


import com.example.appointment.dto.client.DoctorAvailabilityResponse;
import com.example.appointment.dto.client.DoctorResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

        import java.time.LocalDate;

@FeignClient(name = "doctor-service")
public interface DoctorClient {

    @GetMapping("/api/doctors/{id}")
    DoctorResponse getDoctor(
            @PathVariable Long id
    );

    @GetMapping(
            "/api/doctors/{doctorId}/schedules/availability"
    )
    DoctorAvailabilityResponse getAvailability(
            @PathVariable Long doctorId,
            @RequestParam LocalDate date
    );
}