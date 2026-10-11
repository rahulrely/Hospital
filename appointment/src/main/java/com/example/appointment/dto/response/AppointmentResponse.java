package com.example.appointment.dto.response;

import com.example.appointment.entity.AppointmentStatus;
import com.example.appointment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AppointmentResponse(

        Long id,

        Long patientId,

        Long doctorId,

        LocalDate appointmentDate,

        LocalTime appointmentTime,

        AppointmentStatus status,

        PaymentStatus consultationPaymentStatus,

        BigDecimal consultationFee,

        Long paymentId,

        String reason,

        LocalDateTime temporaryBookingExpiresAt,

        LocalDateTime checkedInAt,

        LocalDateTime consultationStartedAt,

        LocalDateTime completedAt,

        LocalDateTime cancelledAt

) {
}
