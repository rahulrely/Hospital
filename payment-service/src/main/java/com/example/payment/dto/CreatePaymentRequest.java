package com.example.payment.dto;

import com.example.payment.entity.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotNull Long appointmentId,
        @NotNull Long patientId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotNull PaymentType type,
        @NotBlank String idempotencyKey
) {}
