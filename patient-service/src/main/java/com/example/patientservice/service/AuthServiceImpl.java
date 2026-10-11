package com.example.patientservice.service;

import com.example.patientservice.dto.AuthResponse;
import com.example.patientservice.dto.LoginRequest;
import com.example.patientservice.dto.SignUpRequest;
import com.example.patientservice.entity.Patient;
import com.example.patientservice.exception.ResourceNotFoundException;
import com.example.patientservice.mapper.PatientMapper;
import com.example.patientservice.repository.PatientRepository;
import com.example.patientservice.Security.AuthUtil;
import com.example.patientservice.Security.JwtUserPrincipal;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AuthServiceImpl implements AuthService {

    PatientRepository patientRepository;
    PatientMapper userMapper;
    PasswordEncoder passwordEncoder;
    AuthUtil authUtil;
    AuthenticationManager authenticationManager;

    @Override
    public AuthResponse signup(SignUpRequest request) {
        patientRepository.findByEmail(request.email()).ifPresent(patient -> {
            throw new ResourceNotFoundException(
                    "Patient",
                    request.email()
            );
        });

        Patient patient = userMapper.toEntity(request);
        patient.setPassword(passwordEncoder.encode(request.password()));
        patient = patientRepository.save(patient);

        log.info("Patient registered successfully: {}" , patient.getFirstName());
        log.info("Patient registered successfully: {}" , patient.getLastName());
//        log.info("Patient registered successfully: {}" , patient.getFirstName());

        JwtUserPrincipal jwtUserPrincipal = new JwtUserPrincipal(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getEmail(),
                null,
                patient.getDateOfBirth().toString(),
                patient.getAge().toString(),
                patient.getGender().toString(),
                patient.getBloodGroup(),
                patient.getPhone(),
                patient.getAddress(),
                new ArrayList<>());

        String token = authUtil.generateAccessToken(jwtUserPrincipal);
        return new AuthResponse(token, userMapper.toPatientProfileResponse(jwtUserPrincipal));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        JwtUserPrincipal patient = (JwtUserPrincipal) authentication.getPrincipal();
        String token = authUtil.generateAccessToken(patient);

        return new AuthResponse(token, userMapper.toPatientProfileResponse(patient));
    }
}