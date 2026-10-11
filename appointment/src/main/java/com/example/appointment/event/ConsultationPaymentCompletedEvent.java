package com.example.appointment.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ConsultationPaymentCompletedEvent(
        String eventId,
        Long paymentId,
        Long appointmentId,
        Long patientId,
        BigDecimal amount,
        String currency,
        String gatewayPaymentId,
        LocalDateTime paidAt
) {
}