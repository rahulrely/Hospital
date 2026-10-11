package com.example.patientservice.Security;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record JwtUserPrincipal(
        Long patientId,
        String firstname,
        String lastname,
        String email,
        String password,
        String dateOfBirth,
        String age,
        String gender,
        String bloodGroup,
        String phone,
        String address,
        List<GrantedAuthority> authorities
) implements UserDetails {
    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public @NonNull String getUsername() {
        return email;
    }
}