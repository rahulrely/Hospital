package com.example.appointment.exception;

public class ErrorResponseException extends RuntimeException {
    public ErrorResponseException(String message) {
        super(message);
    }
}
