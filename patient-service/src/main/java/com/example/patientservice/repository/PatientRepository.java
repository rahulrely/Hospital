package com.example.patientservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.patientservice.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient,Long> {

    Optional<Patient> findByEmail(String email);

    Page<Patient> findByAgeBetween(
            Integer minAge,
            Integer maxAge,
            Pageable pageable
    );

    Page<Patient> findByAgeGreaterThanEqual(
            Integer minAge,
            Pageable pageable
    );

    Page<Patient> findByAgeLessThanEqual(
            Integer maxAge,
            Pageable pageable
    );


}