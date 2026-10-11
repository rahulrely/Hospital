package com.example.payment.kafka;

import com.example.payment.entity.OutboxEvent;
import com.example.payment.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {
    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${app.outbox.poll-ms:1000}")
    public void publishPendingEvents() {
        log.info("Polling for pending outbox events to publish");
        List<OutboxEvent> pending = outboxRepository
                .findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();

        for (OutboxEvent event : pending) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getMessageKey(), event.getPayload())
                        .get(10, TimeUnit.SECONDS);
                markPublished(event.getId());
            } catch (Exception ex) {
                log.warn("Outbox event {} not published; it will be retried", event.getEventId(), ex);
            }
        }
    }

    @Transactional
    public void markPublished(Long id) {
        outboxRepository.findById(id).ifPresent(event -> {
            event.setPublishedAt(LocalDateTime.now());
            outboxRepository.save(event);
        });
    }
}
