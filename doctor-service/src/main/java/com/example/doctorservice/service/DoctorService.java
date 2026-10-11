package com.example.doctorservice.service;

import com.example.doctorservice.dto.request.CreateDoctorRequest;
import com.example.doctorservice.dto.request.UpdateDoctorRequest;
import com.example.doctorservice.dto.response.DoctorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DoctorService {

    DoctorResponse create(CreateDoctorRequest request);

    DoctorResponse getById(Long id);

    Page<DoctorResponse> getAll(Pageable pageable);

    List<DoctorResponse> getBySpecialization(
            String specialization
    );

    DoctorResponse update(
            Long id,
            UpdateDoctorRequest request
    );

    void delete(Long id);
}