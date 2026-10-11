package com.example.patientservice.service.implementation;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.example.patientservice.entity.Patient;
import com.example.patientservice.repository.PatientRepository;
import com.example.patientservice.Security.JwtUserPrincipal;

import java.util.ArrayList;

@RequiredArgsConstructor
@Service
public class PatientService implements UserDetailsService {

    private final PatientRepository patientRepository;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found: " + email));

        return new JwtUserPrincipal(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getEmail(),
                patient.getPassword(),
                patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : null,
                patient.getAge().toString(),
                patient.getGender() != null ? patient.getGender().name() : null,
                patient.getBloodGroup(),
                patient.getPhone(),
                patient.getAddress(),
                new ArrayList<>()
        );    }
}