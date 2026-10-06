package com.example.userservice.service;

import com.example.userservice.dto.AuthResponse;
import com.example.userservice.dto.LoginRequest;
import com.example.userservice.dto.SignUpRequest;
import com.example.userservice.entity.User;
import com.example.userservice.exception.ResourceNotFoundException;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.repository.UserRepository;
//import com.codingshuttle.distributed_lovable.common_lib.error.BadRequestException;
import com.example.userservice.Security.AuthUtil;
import com.example.userservice.Security.JwtUserPrincipal;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    AuthUtil authUtil;
    AuthenticationManager authenticationManager;

    @Override
    public AuthResponse signup(SignUpRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            throw new ResourceNotFoundException(
                    "User",
                    request.email()
            );
        });

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user = userRepository.save(user);

        JwtUserPrincipal jwtUserPrincipal = new JwtUserPrincipal(user.getId(), user.getName(),
                user.getEmail(), null,  new ArrayList<>());

        String token = authUtil.generateAccessToken(jwtUserPrincipal);
        return new AuthResponse(token, userMapper.toUserProfileResponse(jwtUserPrincipal));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        JwtUserPrincipal user = (JwtUserPrincipal) authentication.getPrincipal();
        String token = authUtil.generateAccessToken(user);

        return new AuthResponse(token, userMapper.toUserProfileResponse(user));
    }
}