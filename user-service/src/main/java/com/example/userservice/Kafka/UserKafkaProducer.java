package com.example.userservice.Kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserKafkaProducer {

    private static final String TOPIC = "user-created";

    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    public void sendUserCreatedEvent(UserCreatedEvent event) {

        kafkaTemplate.send(
                TOPIC,
                event.getUserId().toString(),
                event
        );
    }
}