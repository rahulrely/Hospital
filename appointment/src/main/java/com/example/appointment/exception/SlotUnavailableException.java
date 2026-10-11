package com.example.appointment.exception;


public class SlotUnavailableException
        extends RuntimeException {

    public SlotUnavailableException(String message) {
        super(message);
    }
}