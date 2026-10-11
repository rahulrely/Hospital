package com.example.payment.service;

import com.example.payment.dto.CreatePaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.repository.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final StripeCheckoutService stripeCheckoutService;

    public PaymentResponse createPayment(CreatePaymentRequest request) {
        var existing = paymentRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existing.isPresent()) {
            Payment p = existing.get();
            ensureSameRequest(p, request);
            if (p.getCheckoutUrl() != null) return PaymentResponse.from(p);
            throw new IllegalStateException(
                    "Payment already exists but Checkout URL is missing; inspect payment " + p.getId());
        }

        // DEMO NOTE: production must fetch the fee and verify patient/appointment
        // ownership from trusted Appointment/Doctor services; do not trust request.amount.
        Payment payment = Payment.builder()
                .appointmentId(request.appointmentId())
                .patientId(request.patientId())
                .amount(request.amount())
                .currency("inr")
                .type(request.type())
                .status(PaymentStatus.PENDING)
                .idempotencyKey(request.idempotencyKey())
                .build();

        try {
            payment = paymentRepository.saveAndFlush(payment);
        } catch (DataIntegrityViolationException duplicateRace) {
            Payment concurrent = paymentRepository.findByIdempotencyKey(request.idempotencyKey())
                    .orElseThrow(() -> duplicateRace);
            ensureSameRequest(concurrent, request);
            if (concurrent.getCheckoutUrl() != null) return PaymentResponse.from(concurrent);
            throw new IllegalStateException("Concurrent payment creation; retry with the same idempotency key");
        }

        try {
            Session session = stripeCheckoutService.createCheckoutSession(payment);
            payment.setStripeCheckoutSessionId(session.getId());
            payment.setCheckoutUrl(session.getUrl());
            payment = paymentRepository.save(payment);
            return PaymentResponse.from(payment);
        } catch (StripeException e) {
            // Leave a PENDING record for operational reconciliation; do not claim success.
            throw new IllegalStateException("Could not create Stripe Checkout Session for payment "
                    + payment.getId(), e);
        }
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long id) {
        return PaymentResponse.from(paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + id)));
    }

    private void ensureSameRequest(Payment payment, CreatePaymentRequest request) {
        boolean same = Objects.equals(payment.getAppointmentId(), request.appointmentId())
                && Objects.equals(payment.getPatientId(), request.patientId())
                && payment.getAmount().compareTo(request.amount()) == 0
                && payment.getType() == request.type();
        if (!same) {
            throw new IllegalArgumentException(
                    "Idempotency key was already used with different payment details");
        }
    }
}
