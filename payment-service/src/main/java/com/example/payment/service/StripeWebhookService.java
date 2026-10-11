
package com.example.payment.service;

import com.example.payment.entity.OutboxEvent;
import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.entity.ProcessedStripeEvent;
import com.example.payment.event.ConsultationPaymentCompletedEvent;
import com.example.payment.repository.OutboxEventRepository;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.repository.ProcessedStripeEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookService {

    private static final String TOPIC =
            "consultation-payment-completed";

    private final PaymentRepository paymentRepository;
    private final ProcessedStripeEventRepository processedEventRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void handleCheckoutPaid(String stripeEventId, Session session) {

        if (stripeEventId == null || stripeEventId.isBlank()) {
            throw new IllegalArgumentException(
                    "Stripe event ID is required");
        }

        if (session == null || session.getId() == null
                || session.getId().isBlank()) {
            throw new IllegalArgumentException(
                    "Stripe Checkout Session ID is required");
        }

        log.info(
                "Processing Stripe event={}, sessionId={}, paymentStatus={}",
                stripeEventId,
                session.getId(),
                session.getPaymentStatus()
        );

        // Idempotency: Stripe may deliver the same event multiple times.
        if (processedEventRepository.existsById(stripeEventId)) {
            log.info("Stripe event already processed: {}", stripeEventId);
            return;
        }

        // Never mark an unpaid event as processed.
        if (!"paid".equalsIgnoreCase(session.getPaymentStatus())) {
            log.info(
                    "Session is not paid yet. sessionId={}, status={}",
                    session.getId(),
                    session.getPaymentStatus()
            );
            return;
        }

        if (session.getMetadata() == null) {
            throw new IllegalArgumentException(
                    "Checkout Session metadata is missing");
        }

        String paymentIdText = session.getMetadata().get("paymentId");
        String appointmentIdText =
                session.getMetadata().get("appointmentId");
        String patientIdText = session.getMetadata().get("patientId");

        if (paymentIdText == null || appointmentIdText == null
                || patientIdText == null) {
            throw new IllegalArgumentException(
                    "Checkout Session must contain paymentId, "
                            + "appointmentId, and patientId metadata");
        }

        final Long paymentId;

        try {
            paymentId = Long.valueOf(paymentIdText);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Invalid paymentId in Stripe metadata", ex);
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment not found: " + paymentId));

        log.info(
                "Matched payment id={}, status={}, amount={}, currency={}",
                payment.getId(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getCurrency()
        );

        // Verify that this session belongs to the stored payment.
        if (!Objects.equals(
                payment.getStripeCheckoutSessionId(),
                session.getId())) {
            throw new IllegalArgumentException(
                    "Checkout Session does not match stored payment");
        }

        // Verify Stripe metadata against the database.
        if (!Objects.equals(
                String.valueOf(payment.getAppointmentId()),
                appointmentIdText)
                || !Objects.equals(
                String.valueOf(payment.getPatientId()),
                patientIdText)) {
            throw new IllegalArgumentException(
                    "Stripe metadata does not match stored payment");
        }

        if (payment.getAmount() == null
                || payment.getCurrency() == null
                || session.getAmountTotal() == null
                || session.getCurrency() == null) {
            throw new IllegalArgumentException(
                    "Payment amount or currency is missing");
        }

        // Convert the expected amount into the currency's minor units.
        int fractionDigits = Currency.getInstance(
                payment.getCurrency().toUpperCase(Locale.ROOT)
        ).getDefaultFractionDigits();

        if (fractionDigits < 0) {
            throw new IllegalArgumentException(
                    "Unsupported currency: " + payment.getCurrency());
        }

        long expectedAmountMinor;

        try {
            expectedAmountMinor = payment.getAmount()
                    .movePointRight(fractionDigits)
                    .longValueExact();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(
                    "Invalid payment amount precision", ex);
        }

        if (!Objects.equals(
                session.getAmountTotal(),
                expectedAmountMinor)) {
            throw new IllegalArgumentException(
                    "Stripe amount does not match stored payment");
        }

        if (!payment.getCurrency().equalsIgnoreCase(
                session.getCurrency())) {
            throw new IllegalArgumentException(
                    "Stripe currency does not match stored payment");
        }

        // If already successful, don't create another outbox event.
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            processedEventRepository.save(
                    new ProcessedStripeEvent(stripeEventId));

            log.info(
                    "Payment {} is already successful",
                    payment.getId());
            return;
        }

        if (payment.getStatus() != PaymentStatus.PENDING
                && payment.getStatus() != PaymentStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Payment cannot transition to SUCCESS from "
                            + payment.getStatus());
        }

        LocalDateTime paidAt = LocalDateTime.now();

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setGatewayPaymentId(session.getPaymentIntent());
        payment.setPaidAt(paidAt);

        paymentRepository.save(payment);

        ConsultationPaymentCompletedEvent paymentEvent =
                new ConsultationPaymentCompletedEvent(
                        stripeEventId,
                        payment.getId(),
                        payment.getAppointmentId(),
                        payment.getPatientId(),
                        payment.getAmount(),
                        payment.getCurrency(),
                        payment.getGatewayPaymentId(),
                        paidAt
                );

        try {
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .eventId(stripeEventId)
                    .topic(TOPIC)
                    .messageKey(payment.getAppointmentId().toString())
                    .payload(objectMapper.writeValueAsString(paymentEvent))
                    .build();

            outboxEventRepository.save(outboxEvent);

        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(
                    "Could not serialize payment completion event", ex);
        }

        processedEventRepository.save(
                new ProcessedStripeEvent(stripeEventId));

        log.info(
                "Stripe payment successfully processed. paymentId={}, eventId={}",
                payment.getId(),
                stripeEventId
        );
    }
}
