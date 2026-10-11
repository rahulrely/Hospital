package com.example.appointment.service;

import com.example.appointment.client.DoctorClient;
import com.example.appointment.client.PatientClient;
import com.example.appointment.dto.client.DoctorAvailabilityResponse;
import com.example.appointment.dto.client.DoctorResponse;
import com.example.appointment.dto.request.CreateAppointmentRequest;
import com.example.appointment.dto.response.AppointmentResponse;
import com.example.appointment.entity.Appointment;
import com.example.appointment.entity.AppointmentStatus;
import com.example.appointment.entity.PaymentStatus;
import com.example.appointment.exception.ResourceNotFoundException;
import com.example.appointment.exception.SlotUnavailableException;
import com.example.appointment.redis.SlotLockService;
import com.example.appointment.repository.AppointmentRepository;
import com.example.appointment.state.AppointmentEvent;
import com.example.appointment.state.AppointmentStateMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;

    private final DoctorClient doctorClient;

    private final PatientClient patientClient;

    private final SlotLockService slotLockService;

    private final AppointmentStateMachine stateMachine;


    // =========================================================
    // CREATE APPOINTMENT
    // =========================================================

    @Override
    public AppointmentResponse create(
            CreateAppointmentRequest request
    ) {

        /*
         * 1. Validate patient
         */
        patientClient.getPatient(
                request.patientId()
        );


        /*
         * 2. Validate doctor
         */
        DoctorResponse doctor =
                doctorClient.getDoctor(
                        request.doctorId()
                );

        if (!Boolean.TRUE.equals(doctor.active())) {

            throw new SlotUnavailableException(
                    "Doctor is not active"
            );
        }


        /*
         * 3. Validate doctor's working schedule
         */
        DoctorAvailabilityResponse availability =
                doctorClient.getAvailability(
                        request.doctorId(),
                        request.appointmentDate()
                );

        if (!availability.working()) {

            throw new SlotUnavailableException(
                    "Doctor does not work on this date"
            );
        }

        if (!availability.slots().contains(
                request.appointmentTime()
        )) {

            throw new SlotUnavailableException(
                    "Selected time is outside doctor's schedule"
            );
        }


        /*
         * 4. Check PostgreSQL
         *
         * Only active appointment states block the slot.
         */
        boolean alreadyBooked =
                appointmentRepository
                        .existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusIn(
                                request.doctorId(),
                                request.appointmentDate(),
                                request.appointmentTime(),
                                List.of(
                                        AppointmentStatus.TEMPORARILY_BOOKED,
                                        AppointmentStatus.CONFIRMED,
                                        AppointmentStatus.CHECKED_IN,
                                        AppointmentStatus.WAITING,
                                        AppointmentStatus.IN_PROGRESS
                                )
                        );

        if (alreadyBooked) {

            throw new SlotUnavailableException(
                    "Appointment slot is already booked"
            );
        }


        /*
         * 5. Acquire Redis distributed lock
         *
         * Lock duration is handled inside SlotLockService.
         */
        boolean lockAcquired =
                slotLockService.acquire(
                        request.doctorId(),
                        request.appointmentDate(),
                        request.appointmentTime(),
                        request.patientId()
                );

        if (!lockAcquired) {

            throw new SlotUnavailableException(
                    "Slot is currently being booked by another patient"
            );
        }


        /*
         * 6. Create temporary appointment
         *
         * Patient gets 5 minutes to complete consultation payment.
         */
        try {

            Appointment appointment =
                    Appointment.builder()
                            .patientId(
                                    request.patientId()
                            )
                            .doctorId(
                                    request.doctorId()
                            )
                            .appointmentDate(
                                    request.appointmentDate()
                            )
                            .appointmentTime(
                                    request.appointmentTime()
                            )
                            .status(
                                    AppointmentStatus.TEMPORARILY_BOOKED
                            )
                            .consultationPaymentStatus(
                                    PaymentStatus.PENDING
                            )
                            .consultationFee(
                                    doctor.consultationFee()
                            )
                            .reason(
                                    request.reason()
                            )
                            .temporaryBookingExpiresAt(
                                    LocalDateTime.now()
                                            .plusMinutes(5)
                            )
                            .build();


            appointment =
                    appointmentRepository.save(
                            appointment
                    );


            return toResponse(appointment);

        } catch (Exception ex) {

            /*
             * DB save failed.
             * Release Redis lock immediately.
             */
            slotLockService.release(
                    request.doctorId(),
                    request.appointmentDate(),
                    request.appointmentTime()
            );

            throw ex;
        }
    }


    // =========================================================
    // GET APPOINTMENT
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getById(Long appointmentId) {

        Appointment appointment =
                appointmentRepository.findById(
                        appointmentId
                ).orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Appointment not found with id: "
                                        + appointmentId
                        )
                );

        return toResponse(appointment);
    }


    // =========================================================
    // GET ALL APPOINTMENTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getAll(
            Pageable pageable
    ) {

        return appointmentRepository
                .findAll(pageable)
                .map(this::toResponse);
    }


    // =========================================================
    // GET PATIENT APPOINTMENTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPatientAppointments(
            Long patientId
    ) {

        /*
         * Validate patient first.
         */
        patientClient.getPatient(patientId);

        return appointmentRepository
                .findByPatientIdOrderByAppointmentDateDesc(
                        patientId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // =========================================================
    // GET DOCTOR APPOINTMENTS FOR DATE
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDoctorAppointments(
            Long doctorId,
            java.time.LocalDate date
    ) {

        /*
         * Validate doctor.
         */
        doctorClient.getDoctor(doctorId);

        return appointmentRepository
                .findByDoctorIdAndAppointmentDate(
                        doctorId,
                        date
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // =========================================================
    // CHECK IN
    // =========================================================

    @Override
    public AppointmentResponse checkIn(
            Long appointmentId
    ) {

        Appointment appointment =
                getAppointmentEntity(appointmentId);

        stateMachine.transition(
                appointment,
                AppointmentEvent.PATIENT_CHECKED_IN
        );

        appointment.setCheckedInAt(
                LocalDateTime.now()
        );

        appointment =
                appointmentRepository.save(
                        appointment
                );

        return toResponse(appointment);
    }


    // =========================================================
    // JOIN QUEUE
    // =========================================================

    @Override
    public AppointmentResponse queue(
            Long appointmentId
    ) {

        Appointment appointment =
                getAppointmentEntity(appointmentId);

        stateMachine.transition(
                appointment,
                AppointmentEvent.QUEUE_JOINED
        );

        appointment =
                appointmentRepository.save(
                        appointment
                );

        return toResponse(appointment);
    }

    @Override
    public AppointmentResponse startConsultation(Long id) {
        return null;
    }

    @Override
    public AppointmentResponse completeConsultation(Long id) {
        return null;
    }


    // =========================================================
    // START CONSULTATION
    // =========================================================

    @Override
    public AppointmentResponse start(
            Long appointmentId
    ) {

        Appointment appointment =
                getAppointmentEntity(appointmentId);

        stateMachine.transition(
                appointment,
                AppointmentEvent.CONSULTATION_STARTED
        );

        appointment.setConsultationStartedAt(
                LocalDateTime.now()
        );

        appointment =
                appointmentRepository.save(
                        appointment
                );

        return toResponse(appointment);
    }


    // =========================================================
    // COMPLETE CONSULTATION
    // =========================================================

    @Override
    public AppointmentResponse complete(
            Long appointmentId
    ) {

        Appointment appointment =
                getAppointmentEntity(appointmentId);

        stateMachine.transition(
                appointment,
                AppointmentEvent.CONSULTATION_COMPLETED
        );

        appointment.setCompletedAt(
                LocalDateTime.now()
        );

        /*
         * Release Redis slot after consultation completion.
         */
        slotLockService.release(
                appointment.getDoctorId(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime()
        );

        appointment =
                appointmentRepository.save(
                        appointment
                );

        return toResponse(appointment);
    }


    // =========================================================
    // CANCEL APPOINTMENT
    // =========================================================

    @Override
    public AppointmentResponse cancel(
            Long appointmentId,
            String reason
    ) {

        Appointment appointment =
                getAppointmentEntity(appointmentId);

        stateMachine.transition(
                appointment,
                AppointmentEvent.PATIENT_CANCELLED
        );

        appointment.setCancellationReason(
                reason
        );

        appointment.setCancelledAt(
                LocalDateTime.now()
        );

        /*
         * Release Redis lock.
         */
        slotLockService.release(
                appointment.getDoctorId(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime()
        );

        appointment =
                appointmentRepository.save(
                        appointment
                );

        return toResponse(appointment);
    }


    // =========================================================
    // MARK NO SHOW
    // =========================================================

    @Override
    public AppointmentResponse noShow(
            Long appointmentId
    ) {

        Appointment appointment =
                getAppointmentEntity(appointmentId);

        stateMachine.transition(
                appointment,
                AppointmentEvent.PATIENT_NO_SHOW
        );

        /*
         * Release Redis slot.
         */
        slotLockService.release(
                appointment.getDoctorId(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime()
        );

        appointment =
                appointmentRepository.save(
                        appointment
                );

        return toResponse(appointment);
    }


    // =========================================================
    // EXPIRE TEMPORARY BOOKINGS
    // =========================================================

    @Scheduled(fixedDelay = 30_000)
    public void expireTemporaryBookings() {

        List<Appointment> expiredAppointments =
                appointmentRepository
                        .findByStatusAndTemporaryBookingExpiresAtBefore(
                                AppointmentStatus.TEMPORARILY_BOOKED,
                                LocalDateTime.now()
                        );

        for (Appointment appointment : expiredAppointments) {

            /*
             * Change state.
             */
            stateMachine.transition(
                    appointment,
                    AppointmentEvent.PAYMENT_TIMEOUT
            );


            /*
             * Release Redis lock.
             */
            slotLockService.release(
                    appointment.getDoctorId(),
                    appointment.getAppointmentDate(),
                    appointment.getAppointmentTime()
            );


            /*
             * Save EXPIRED appointment.
             */
            appointmentRepository.save(
                    appointment
            );
        }
    }


    // =========================================================
    // FIND ENTITY
    // =========================================================

    private Appointment getAppointmentEntity(
            Long appointmentId
    ) {

        return appointmentRepository.findById(
                appointmentId
        ).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Appointment not found with id: "
                                + appointmentId
                )
        );
    }


    // =========================================================
    // ENTITY -> RESPONSE
    // =========================================================

    private AppointmentResponse toResponse(
            Appointment appointment
    ) {

        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatientId(),
                appointment.getDoctorId(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getStatus(),
                appointment.getConsultationPaymentStatus(),
                appointment.getConsultationFee(),
                appointment.getPaymentId(),
                appointment.getReason(),
                appointment.getTemporaryBookingExpiresAt(),
                appointment.getCheckedInAt(),
                appointment.getConsultationStartedAt(),
                appointment.getCompletedAt(),
                appointment.getCancelledAt()
        );
    }
}