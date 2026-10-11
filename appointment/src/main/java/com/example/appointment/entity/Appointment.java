package com.example.appointment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
        name = "appointments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_doctor_slot",
                        columnNames = {
                                "doctor_id",
                                "appointment_date",
                                "appointment_time"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_patient_appointments",
                        columnList = "patient_id"
                ),
                @Index(
                        name = "idx_doctor_appointments",
                        columnList = "doctor_id,appointment_date"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "patient_id",
            nullable = false
    )
    private Long patientId;

    @Column(
            name = "doctor_id",
            nullable = false
    )
    private Long doctorId;

    @Column(
            name = "appointment_date",
            nullable = false
    )
    private LocalDate appointmentDate;

    @Column(
            name = "appointment_time",
            nullable = false
    )
    private LocalTime appointmentTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus consultationPaymentStatus;

    @Column(
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal consultationFee;

    private Long paymentId;

    private String reason;

    private String cancellationReason;

    private LocalDateTime temporaryBookingExpiresAt;

    private LocalDateTime checkedInAt;

    private LocalDateTime consultationStartedAt;

    private LocalDateTime completedAt;

    private LocalDateTime cancelledAt;

    @Version
    private Long version;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}