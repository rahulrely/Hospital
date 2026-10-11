
package com.example.patientservice.service;

import com.example.patientservice.Kafka.PatientCreatedEvent;
import com.example.patientservice.Kafka.PatientKafkaProducer;
import com.example.patientservice.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import com.example.patientservice.dto.PatientDto;
import com.example.patientservice.entity.Patient;


import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientKafkaProducer patientKafkaProducer;

    @Override
    public PatientDto createPatient(PatientDto patientDto) {

        Patient patient = Patient.builder()
                .firstName(patientDto.getFirstName())
                .lastName(patientDto.getLastName())
                .email(patientDto.getEmail())
                .password(patientDto.getPassword())
                .dateOfBirth(patientDto.getDateOfBirth())
                .age(patientDto.getAge())
                .gender(patientDto.getGender())
                .bloodGroup(patientDto.getBloodGroup())
                .phone(patientDto.getPhone())
                .address(patientDto.getAddress())
                .build();

        Patient savedPatient = patientRepository.save(patient);

        PatientCreatedEvent event = PatientCreatedEvent.builder()
                .firstName(patientDto.getFirstName())
                .lastName(patientDto.getLastName())
                .email(patientDto.getEmail())
//               NULL for security reasons, we don't want to send the password in the event
                .dateOfBirth(patientDto.getDateOfBirth())
                .age(patientDto.getAge())
                .gender(patientDto.getGender().toString())
                .bloodGroup(patientDto.getBloodGroup())
                .phone(patientDto.getPhone())
                .address(patientDto.getAddress())
                .build();

        patientKafkaProducer.sendPatientCreatedEvent(event);

        return convertToDto(savedPatient);
    }

    @Override
    public List<PatientDto> getAllPatients() {

        return patientRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public PatientDto getPatientById(Long id) {

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Patient not found with id: " + id));

        return convertToDto(patient);
    }

    @Override
    public PatientDto updatePatient(Long id, PatientDto patientDto) {

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Patient not found with id: " + id));

        if(patientDto.getFirstName() != null) patient.setFirstName(patientDto.getFirstName());
        if(patientDto.getLastName() != null) patient.setLastName(patientDto.getLastName());
        if(patientDto.getEmail() != null) patient.setEmail(patientDto.getEmail());
        if(patientDto.getPassword()!= null) patient.setPassword(patientDto.getPassword());
        if(patientDto.getAge() != null) patient.setAge(patientDto.getAge());

        Patient updatedPatient = patientRepository.save(patient);

        return convertToDto(updatedPatient);
    }

    @Override
    public void deletePatient(Long id) {

        if (!patientRepository.existsById(id)) {
            throw new RuntimeException("Patient not found with id: " + id);
        }

        patientRepository.deleteById(id);
    }

    private PatientDto convertToDto(Patient patient) {

        return PatientDto.builder()
                .id(patient.getId())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .email(patient.getEmail())
                .password(patient.getPassword())
                .dateOfBirth(patient.getDateOfBirth())
                .age(patient.getAge())
                .gender(patient.getGender())
                .bloodGroup(patient.getBloodGroup())
                .phone(patient.getPhone())
                .address(patient.getAddress())
                .build();
    }
    @Override
    public Page<PatientDto> searchPatients(
            Integer minAge,
            Integer maxAge,
            int page,
            int size,
            String sortDirection) {

        Sort sort = Sort.by(
                Sort.Direction.fromString(sortDirection),
                "createdAt"
        );

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Patient> patients;

        if (minAge != null && maxAge != null) {

            patients = patientRepository.findByAgeBetween(
                    minAge,
                    maxAge,
                    pageable
            );

        } else if (minAge != null) {

            patients = patientRepository.findByAgeGreaterThanEqual(
                    minAge,
                    pageable
            );

        } else if (maxAge != null) {

            patients = patientRepository.findByAgeLessThanEqual(
                    maxAge,
                    pageable
            );

        } else {

            patients = patientRepository.findAll(pageable);
        }

        return patients.map(this::convertToDto);
    }
}