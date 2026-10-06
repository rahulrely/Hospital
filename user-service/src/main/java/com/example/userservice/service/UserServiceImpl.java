
package com.example.userservice.service;

import com.example.userservice.Kafka.UserCreatedEvent;
import com.example.userservice.Kafka.UserKafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import com.example.userservice.dto.UserDto;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;


import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserKafkaProducer userKafkaProducer;

    @Override
    public UserDto createUser(UserDto userDto) {

        User user = User.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .password(userDto.getPassword())
                .age(userDto.getAge())
                .build();

        User savedUser = userRepository.save(user);
        UserCreatedEvent event = UserCreatedEvent.builder()
                .userId(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .age(savedUser.getAge())
                .build();

        userKafkaProducer.sendUserCreatedEvent(event);

        return convertToDto(savedUser);
    }

    @Override
    public List<UserDto> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public UserDto getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found with id: " + id));

        return convertToDto(user);
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found with id: " + id));

        if(userDto.getName() != null) user.setName(userDto.getName());
        if(userDto.getEmail() != null) user.setEmail(userDto.getEmail());
        if(userDto.getPassword()!= null) user.setPassword(userDto.getPassword());
        if(userDto.getAge() != null) user.setAge(userDto.getAge());

        User updatedUser = userRepository.save(user);

        return convertToDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {

        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
    }

    private UserDto convertToDto(User user) {

        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .password(user.getPassword())
                .age(user.getAge())
                .build();
    }
    @Override
    public Page<UserDto> searchUsers(
            Integer minAge,
            Integer maxAge,
            int page,
            int size,
            String sortDirection) {

        Sort sort = Sort.by(
                Sort.Direction.fromString(sortDirection),
                "createdAt"
        );

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> users;

        if (minAge != null && maxAge != null) {

            users = userRepository.findByAgeBetween(
                    minAge,
                    maxAge,
                    pageable
            );

        } else if (minAge != null) {

            users = userRepository.findByAgeGreaterThanEqual(
                    minAge,
                    pageable
            );

        } else if (maxAge != null) {

            users = userRepository.findByAgeLessThanEqual(
                    maxAge,
                    pageable
            );

        } else {

            users = userRepository.findAll(pageable);
        }

        return users.map(this::convertToDto);
    }
}