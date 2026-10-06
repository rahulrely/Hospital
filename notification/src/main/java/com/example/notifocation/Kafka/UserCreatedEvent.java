package com.example.notifocation.Kafka;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreatedEvent {

    private Long userId;
    private String name;
    private String email;
    private String phone;
    private Integer age;
}