package com.example.patientservice.service;

import com.example.patientservice.dto.AuthResponse;
import com.example.patientservice.dto.LoginRequest;
import com.example.patientservice.dto.SignUpRequest;

public interface AuthService {
    AuthResponse signup(SignUpRequest request);

    AuthResponse login(LoginRequest request);
}