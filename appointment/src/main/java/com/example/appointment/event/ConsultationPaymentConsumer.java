package com.example.appointment.event;


import com.example.appointment.entity.Appointment;
import com.example.appointment.entity.AppointmentStatus;
import com.example.appointment.entity.PaymentStatus;
import com.example.appointment.repository.AppointmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsultationPaymentConsumer {

    private final ObjectMapper objectMapper;
    private final AppointmentRepository appointmentRepository;

    @KafkaListener(
            topics = "consultation-payment-completed",
            groupId = "appointment-service"
    )

    public void consume(String message) throws Exception {

        log.info("========== KAFKA MESSAGE RECEIVED ==========");
        log.info("Topic: consultation-payment-completed");
        log.info("Payload: {}", message);

        ConsultationPaymentCompletedEvent event =
                objectMapper.readValue(
                        message,
                        ConsultationPaymentCompletedEvent.class
                );

        log.info(
                "Received payment event: eventId={}, appointmentId={}, paymentId={}",
                event.eventId(),
                event.appointmentId(),
                event.paymentId()
        );

        if (event.eventId() == null
                || event.paymentId() == null
                || event.appointmentId() == null
                || event.patientId() == null
                || event.amount() == null
                || event.amount().signum() <= 0
                || event.currency() == null
                || !"INR".equalsIgnoreCase(event.currency())
                || event.gatewayPaymentId() == null
                || event.paidAt() == null) {

            throw new IllegalArgumentException(
                    "Invalid consultation payment event"
            );
        }

        Appointment appointment = appointmentRepository
                .findById(event.appointmentId())
                .orElseThrow(() -> new IllegalStateException(
                        "Appointment not found: " + event.appointmentId()
                ));

        // Validate that this payment belongs to this patient.
        if (!appointment.getPatientId().equals(event.patientId())) {
            throw new IllegalStateException(
                    "Patient does not match appointment"
            );
        }

        // Validate the amount against the fee stored by Appointment Service.
        if (appointment.getConsultationFee() == null
                || appointment.getConsultationFee()
                .compareTo(event.amount()) != 0) {

            throw new IllegalStateException(
                    "Payment amount does not match consultation fee"
            );
        }

        // Idempotency: a repeated event must not change the result.
        if (appointment.getStatus() == AppointmentStatus.CONFIRMED
                && event.paymentId().equals(appointment.getPaymentId())
                && appointment.getConsultationPaymentStatus()
                == PaymentStatus.SUCCESS) {

            log.info(
                    "Payment event already applied for appointment {}",
                    appointment.getId()
            );
            return;
        }

        // A late payment must never revive an expired or cancelled booking.
        if (appointment.getStatus() != AppointmentStatus.TEMPORARILY_BOOKED) {
            log.error(
                    "Payment received for appointment {} in state {}. " +
                            "Do not confirm; compensation is required.",
                    appointment.getId(),
                    appointment.getStatus()
            );

            // TODO: persist a durable refund/void compensation request.
            // Do not mark this event as successfully handled without
            // recording how the late payment will be compensated.
            throw new IllegalStateException(
                    "Appointment is no longer temporarily booked"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        if (appointment.getTemporaryBookingExpiresAt() == null
                || !appointment.getTemporaryBookingExpiresAt().isAfter(now)) {

            log.error(
                    "Payment arrived after booking expiry for appointment {}",
                    appointment.getId()
            );

            // TODO: persist a durable refund/void compensation request.
            throw new IllegalStateException(
                    "Temporary booking has expired"
            );
        }

        // Record payment and confirm the valid appointment.
        appointment.setPaymentId(event.paymentId());
        appointment.setConsultationPaymentStatus(PaymentStatus.SUCCESS);
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        appointmentRepository.save(appointment);

        log.info(
                "Appointment {} confirmed after successful payment {}",
                appointment.getId(),
                event.paymentId()
        );
    }
}