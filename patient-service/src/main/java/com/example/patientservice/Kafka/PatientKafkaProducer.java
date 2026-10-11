package com.example.patientservice.Kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PatientKafkaProducer {

    private static final String TOPIC = "patient-created";

    private final KafkaTemplate<String, PatientCreatedEvent> kafkaTemplate;

    public void sendPatientCreatedEvent(PatientCreatedEvent event) {

        kafkaTemplate.send(
                TOPIC,
                event.getPatientId().toString(),
                event
        );
    }
}