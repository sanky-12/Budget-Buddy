package com.budgetbuddy.auth_service.exception;

import org.springframework.http.HttpStatus;
import com.budgetbuddy.auth_service.dto.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidCredentials(InvalidCredentialsException ex) {
        return new ErrorResponse("AUTH_FAILED", ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFound(UserNotFoundException ex) {
        return new ErrorResponse("USER_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleEmailInUse(EmailAlreadyUsedException ex) {
        return new ErrorResponse("EMAIL_IN_USE", ex.getMessage());
    }

    // Fallback for any other uncaught exceptions
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAll(Exception ex) {
        // Optionally log ex here
        return new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred.");
    }
}
