package com.example.userservice.exception;

public class InvaildStateTransitionException extends RuntimeException {
    public InvaildStateTransitionException(String message) {
        super(message);
    }
}
