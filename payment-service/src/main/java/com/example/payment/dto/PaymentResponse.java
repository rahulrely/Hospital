package com.example.payment.dto;

import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.entity.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long appointmentId,
        Long patientId,
        BigDecimal amount,
        String currency,
        PaymentType type,
        PaymentStatus status,
        String stripeCheckoutSessionId,
        String checkoutUrl,
        String gatewayPaymentId,
        LocalDateTime createdAt,
        LocalDateTime paidAt,
        LocalDateTime refundedAt
) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getAppointmentId(),
                p.getPatientId(),
                p.getAmount(),
                p.getCurrency(),
                p.getType(),
                p.getStatus(),
                p.getStripeCheckoutSessionId(),
                p.getCheckoutUrl(),
                p.getGatewayPaymentId(),
                p.getCreatedAt(),
                p.getPaidAt(),
                p.getRefundedAt()
        );
    }
}