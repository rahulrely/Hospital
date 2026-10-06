package com.example.userservice.service;


import com.example.userservice.dto.UserDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {

    UserDto createUser(UserDto userDto);

    List<UserDto> getAllUsers();

    UserDto getUserById(Long id);

    UserDto updateUser(Long id, UserDto userDto);

    void deleteUser(Long id);

    Page<UserDto> searchUsers(
            Integer minAge,
            Integer maxAge,
            int page,
            int size,
            String sortDirection
    );
}
