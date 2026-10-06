package com.example.notifocation.Kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserCreatedConsumer {

    @KafkaListener(
            topics = "user-created",
            groupId = "notification-service"
    )
    public void consume(UserCreatedEvent event) {

        log.info(
                "User created event received: {}",
                event
        );

        // Later we'll actually send email/SMS
        log.info(
                "Sending welcome notification to {}",
                event.getEmail()
        );
    }
}