package com.example.appointment.service;

import com.example.appointment.entity.AppointmentStatus;
import com.example.appointment.redis.SlotLockService;
import com.example.appointment.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AppointmentExpirationJob {

    private final AppointmentRepository appointmentRepository;

    private final SlotLockService slotLockService;

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void expireTemporaryAppointments() {

        var expired =
                appointmentRepository
                        .findByStatusAndTemporaryBookingExpiresAtBefore(
                                AppointmentStatus.TEMPORARILY_BOOKED,
                                LocalDateTime.now()
                        );

        for (var appointment : expired) {

            appointment.setStatus(
                    AppointmentStatus.EXPIRED
            );

            slotLockService.release(
                    appointment.getDoctorId(),
                    appointment.getAppointmentDate(),
                    appointment.getAppointmentTime()
            );
        }
    }
}