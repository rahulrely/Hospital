package com.example.payment.repository;

import com.example.payment.entity.ProcessedStripeEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedStripeEventRepository
        extends JpaRepository<ProcessedStripeEvent, String> {
}
