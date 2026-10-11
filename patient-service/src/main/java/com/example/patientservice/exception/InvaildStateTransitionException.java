package com.example.patientservice.exception;

public class InvaildStateTransitionException extends RuntimeException {
    public InvaildStateTransitionException(String message) {
        super(message);
    }
}
