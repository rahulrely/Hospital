package com.example.appointment.repository;


import com.example.appointment.entity.Appointment;
import com.example.appointment.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusIn(
            Long doctorId,
            LocalDate date,
            LocalTime time,
            List<AppointmentStatus> statuses
    );

    List<Appointment> findByPatientIdOrderByAppointmentDateDesc(
            Long patientId
    );

    List<Appointment> findByDoctorIdAndAppointmentDate(
            Long doctorId,
            LocalDate date
    );

    List<Appointment> findByStatusAndTemporaryBookingExpiresAtBefore(
            AppointmentStatus status,
            java.time.LocalDateTime time
    );
}