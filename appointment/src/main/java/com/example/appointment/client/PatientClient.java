package com.example.appointment.client;


import com.example.appointment.dto.client.PatientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "patient-service")
public interface PatientClient {

    @GetMapping("/api/patients/{id}")
    PatientResponse getPatient(
            @PathVariable Long id
    );
}