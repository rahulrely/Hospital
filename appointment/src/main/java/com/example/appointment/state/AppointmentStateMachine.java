package com.example.appointment.state;

import com.example.appointment.entity.Appointment;
import com.example.appointment.entity.AppointmentStatus;
import com.example.appointment.exception.InvalidAppointmentStateException;
import org.springframework.stereotype.Component;

@Component
public class AppointmentStateMachine {

    public void transition(
            Appointment appointment,
            AppointmentEvent event
    ) {

        AppointmentStatus current =
                appointment.getStatus();

        AppointmentStatus next =
                resolve(current, event);

        appointment.setStatus(next);
    }

    private AppointmentStatus resolve(
            AppointmentStatus state,
            AppointmentEvent event
    ) {

        return switch (state) {

            case TEMPORARILY_BOOKED ->
                    switch (event) {

                        case PAYMENT_SUCCESS ->
                                AppointmentStatus.CONFIRMED;

                        case PAYMENT_FAILED,
                             PAYMENT_TIMEOUT ->
                                AppointmentStatus.EXPIRED;

                        case PATIENT_CANCELLED ->
                                AppointmentStatus.CANCELLED;

                        default ->
                                invalid(state, event);
                    };

            case CONFIRMED ->
                    switch (event) {

                        case PATIENT_CHECKED_IN ->
                                AppointmentStatus.CHECKED_IN;

                        case PATIENT_CANCELLED,
                             DOCTOR_CANCELLED ->
                                AppointmentStatus.CANCELLED;

                        case PATIENT_NO_SHOW ->
                                AppointmentStatus.NO_SHOW;

                        default ->
                                invalid(state, event);
                    };

            case CHECKED_IN ->
                    switch (event) {

                        case QUEUE_JOINED ->
                                AppointmentStatus.WAITING;

                        case PATIENT_NO_SHOW ->
                                AppointmentStatus.NO_SHOW;

                        default ->
                                invalid(state, event);
                    };

            case WAITING ->
                    switch (event) {

                        case CONSULTATION_STARTED ->
                                AppointmentStatus.IN_PROGRESS;

                        default ->
                                invalid(state, event);
                    };

            case IN_PROGRESS ->
                    switch (event) {

                        case CONSULTATION_COMPLETED ->
                                AppointmentStatus.COMPLETED;

                        default ->
                                invalid(state, event);
                    };

            case COMPLETED,
                 CANCELLED,
                 EXPIRED,
                 NO_SHOW ->
                    invalid(state, event);
        };
    }

    private AppointmentStatus invalid(
            AppointmentStatus state,
            AppointmentEvent event
    ) {

        throw new InvalidAppointmentStateException(
                "Cannot apply event " + event +
                        " when appointment is in state " + state
        );
    }
}