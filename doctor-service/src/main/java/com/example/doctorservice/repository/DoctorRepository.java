package com.example.doctorservice.repository;

import com.example.doctorservice.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository
        extends JpaRepository<Doctor, Long> {

    boolean existsByEmail(String email);

    Optional<Doctor> findByEmail(String email);

    List<Doctor> findBySpecializationIgnoreCaseAndActiveTrue(
            String specialization
    );
}