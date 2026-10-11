package com.example.patientservice.dto;

import com.example.patientservice.Enum.Gender;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String email; // Unique identifier for the patient
    private String password;
    private LocalDate dateOfBirth;
    private Integer age;
    private Gender gender;
    private String bloodGroup;
    private String phone;
    private String address;
}