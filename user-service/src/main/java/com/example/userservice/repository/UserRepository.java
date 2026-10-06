package com.example.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.userservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);

    Page<User> findByAgeBetween(
            Integer minAge,
            Integer maxAge,
            Pageable pageable
    );

    Page<User> findByAgeGreaterThanEqual(
            Integer minAge,
            Pageable pageable
    );

    Page<User> findByAgeLessThanEqual(
            Integer maxAge,
            Pageable pageable
    );


}