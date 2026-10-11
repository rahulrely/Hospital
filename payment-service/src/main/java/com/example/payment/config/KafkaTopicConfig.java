package com.example.payment.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Bean
    NewTopic consultationPaymentCompletedTopic(
            @Value("${app.kafka.payment-topic:consultation-payment-completed}") String topic) {
        // Replication factor 3 requires a 3-broker Kafka cluster.
        return TopicBuilder.name(topic).partitions(3).replicas(3).build();
    }
}
