package com.example.userservice.dto;

public record UserProfileResponse(
        Long id,
        String email,
        String name
) {
}