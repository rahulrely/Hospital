package com.example.userservice.service;

import com.example.userservice.dto.AuthResponse;
import com.example.userservice.dto.LoginRequest;
import com.example.userservice.dto.SignUpRequest;

public interface AuthService {
    AuthResponse signup(SignUpRequest request);

    AuthResponse login(LoginRequest request);
}