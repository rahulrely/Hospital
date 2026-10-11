package com.example.patientservice.service;


import com.example.patientservice.dto.PatientDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PatientService {

    PatientDto createPatient(PatientDto patientDto);

    List<PatientDto> getAllPatients();

    PatientDto getPatientById(Long id);

    PatientDto updatePatient(Long id, PatientDto patientDto);

    void deletePatient(Long id);

    Page<PatientDto> searchPatients(
            Integer minAge,
            Integer maxAge,
            int page,
            int size,
            String sortDirection
    );
}
