package com.example.patientservice.controller;

import com.example.patientservice.dto.PatientDto;
import com.example.patientservice.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    public ResponseEntity<PatientDto> createPatient(
            @RequestBody PatientDto patientDto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(patientService.createPatient(patientDto));
    }

    @GetMapping
    public ResponseEntity<List<PatientDto>> getAllPatient() {

        return ResponseEntity.ok(
                patientService.getAllPatients()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientDto> getPatientById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                patientService.getPatientById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientDto> updatePatient(
            @PathVariable Long id,
            @RequestBody PatientDto patientDto) {

        return ResponseEntity.ok(
                patientService.updatePatient(id, patientDto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(
            @PathVariable Long id) {

        patientService.deletePatient(id);

        return ResponseEntity.noContent().build();
    }
    @GetMapping("/search")
    public ResponseEntity<Page<PatientDto>> searchPatients(

            @RequestParam(required = false)
            Integer minAge,

            @RequestParam(required = false)
            Integer maxAge,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "desc")
            String sortDirection
    ) {

        return ResponseEntity.ok(
                patientService.searchPatients(
                        minAge,
                        maxAge,
                        page,
                        size,
                        sortDirection
                )
        );
    }
}