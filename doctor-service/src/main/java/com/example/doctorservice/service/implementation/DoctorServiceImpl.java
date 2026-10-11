package com.example.doctorservice.service.implementation;

import com.example.doctorservice.dto.request.CreateDoctorRequest;
import com.example.doctorservice.dto.request.UpdateDoctorRequest;
import com.example.doctorservice.dto.response.DoctorResponse;
import com.example.doctorservice.entity.Doctor;
import com.example.doctorservice.exception.ResourceAlreadyExistsException;
import com.example.doctorservice.exception.ResourceNotFoundException;
import com.example.doctorservice.repository.DoctorRepository;
import com.example.doctorservice.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    @Override
    public DoctorResponse create(CreateDoctorRequest request) {

        if (doctorRepository.existsByEmail(request.email())) {
            throw new ResourceAlreadyExistsException(
                    "Doctor with email already exists"
            );
        }

        Doctor doctor = Doctor.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .phone(request.phone())
                .specialization(request.specialization())
                .qualification(request.qualification())
                .experienceYears(request.experienceYears())
                .consultationFee(request.consultationFee())
                .active(true)
                .build();

        doctor = doctorRepository.save(doctor);

        return toResponse(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponse getById(Long id) {

        Doctor doctor = findDoctor(id);

        return toResponse(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorResponse> getAll(Pageable pageable) {

        return doctorRepository
                .findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorResponse> getBySpecialization(
            String specialization
    ) {

        return doctorRepository
                .findBySpecializationIgnoreCaseAndActiveTrue(
                        specialization
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public DoctorResponse update(
            Long id,
            UpdateDoctorRequest request
    ) {

        Doctor doctor = findDoctor(id);

        doctor.setFirstName(request.firstName());
        doctor.setLastName(request.lastName());
        doctor.setPhone(request.phone());
        doctor.setSpecialization(request.specialization());
        doctor.setQualification(request.qualification());
        doctor.setExperienceYears(request.experienceYears());
        doctor.setConsultationFee(request.consultationFee());
        doctor.setActive(request.active());

        return toResponse(doctor);
    }

    @Override
    public void delete(Long id) {

        Doctor doctor = findDoctor(id);

        doctor.setActive(false);
    }

    private Doctor findDoctor(Long id) {

        return doctorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor",
                                id
                        ));
    }

    private DoctorResponse toResponse(Doctor doctor) {

        return new DoctorResponse(
                doctor.getId(),
                doctor.getFirstName(),
                doctor.getLastName(),
                doctor.getEmail(),
                doctor.getPhone(),
                doctor.getSpecialization(),
                doctor.getQualification(),
                doctor.getExperienceYears(),
                doctor.getConsultationFee(),
                doctor.getActive()
        );
    }
}