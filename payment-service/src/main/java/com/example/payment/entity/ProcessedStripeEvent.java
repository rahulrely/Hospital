package com.example.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_stripe_events")
@Getter
@Setter
@NoArgsConstructor
public class ProcessedStripeEvent {
    @Id
    @Column(name = "stripe_event_id", length = 255)
    private String stripeEventId;

    @Column(nullable = false)
    private LocalDateTime processedAt;

    public ProcessedStripeEvent(String stripeEventId) {
        this.stripeEventId = stripeEventId;
        this.processedAt = LocalDateTime.now();
    }
}
