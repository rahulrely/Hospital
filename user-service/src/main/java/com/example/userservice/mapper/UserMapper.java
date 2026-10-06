package com.example.userservice.mapper;

import com.example.userservice.dto.SignUpRequest;
import com.example.userservice.dto.UserProfileResponse;
import com.example.userservice.entity.User;
import com.example.userservice.dto.UserDto;
import com.example.userservice.Security.JwtUserPrincipal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(SignUpRequest signupRequest);

    @Mapping(source = "userId", target = "id")
    UserProfileResponse toUserProfileResponse(JwtUserPrincipal user);

    UserDto toUserDto(User user);
}