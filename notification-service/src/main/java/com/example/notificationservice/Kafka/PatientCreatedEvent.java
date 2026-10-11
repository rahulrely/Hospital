package com.example.notificationservice.Kafka;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientCreatedEvent {

    private Long patientId;
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String gender;
    private String bloodGroup;
    private String address;
    private String email;
    private String phone;
    private Integer age;
}