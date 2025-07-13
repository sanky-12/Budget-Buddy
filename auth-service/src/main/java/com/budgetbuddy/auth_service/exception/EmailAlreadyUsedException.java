package com.budgetbuddy.auth_service.exception;

public class EmailAlreadyUsedException extends RuntimeException {
    public EmailAlreadyUsedException() {
        super("Email already in use.");
    }
}