package com.example.notificationservice.Kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PatientCreatedConsumer {

    @KafkaListener(
            topics = "patient-created",
            groupId = "notification-service"
    )
    public void consume(PatientCreatedEvent event) {

        log.info(
                "Patient created event received: {}",
                event
        );

        // Later we'll actually send email/SMS
        log.info(
                "Sending welcome notification to {}",
                event.getEmail()
        );
    }
}