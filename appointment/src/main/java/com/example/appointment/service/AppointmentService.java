package com.example.appointment.service;

import com.example.appointment.dto.request.CreateAppointmentRequest;
import com.example.appointment.dto.response.AppointmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    /*
     * Create a temporary appointment.
     *
     * Initial status:
     * TEMPORARILY_BOOKED
     */
    AppointmentResponse create(
            CreateAppointmentRequest request
    );


    /*
     * Get appointment by ID.
     */
    AppointmentResponse getById(
            Long id
    );


    /*
     * Get all appointments with pagination.
     */
    Page<AppointmentResponse> getAll(
            Pageable pageable
    );


    /*
     * Get all appointments of a patient.
     */
    List<AppointmentResponse> getPatientAppointments(
            Long patientId
    );


    /*
     * Get all appointments of a doctor for a particular date.
     */
    List<AppointmentResponse> getDoctorAppointments(
            Long doctorId,
            LocalDate date
    );


    /*
     * Patient checks in.
     *
     * CONFIRMED
     *      ↓
     * CHECKED_IN
     */
    AppointmentResponse checkIn(
            Long id
    );


    /*
     * Patient joins the queue.
     *
     * CHECKED_IN
     *      ↓
     * WAITING
     */
    AppointmentResponse queue(
            Long id
    );


    /*
     * Doctor starts consultation.
     *
     * WAITING
     *      ↓
     * IN_PROGRESS
     */
    AppointmentResponse startConsultation(
            Long id
    );


    /*
     * Doctor completes consultation.
     *
     * IN_PROGRESS
     *      ↓
     * COMPLETED
     */
    AppointmentResponse completeConsultation(
            Long id
    );


    AppointmentResponse start(
            Long appointmentId
    );

    AppointmentResponse complete(
            Long appointmentId
    );

    /*
     * Cancel appointment.
     */
    AppointmentResponse cancel(
            Long id,
            String reason
    );


    /*
     * Mark patient as no-show.
     */
    AppointmentResponse noShow(
            Long id
    );
}