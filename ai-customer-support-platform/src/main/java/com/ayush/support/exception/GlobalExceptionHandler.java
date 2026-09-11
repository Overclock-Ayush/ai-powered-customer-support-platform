package com.ayush.support.exception;

import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public org.springframework.http.ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst().map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Validation failed");
        return response(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public org.springframework.http.ResponseEntity<?> handleConstraint(ConstraintViolationException ex) {
        return response(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public org.springframework.http.ResponseEntity<?> handleBadCredentials(BadCredentialsException ex) {
        return response(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public org.springframework.http.ResponseEntity<?> handleDenied(AccessDeniedException ex) {
        return response(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public org.springframework.http.ResponseEntity<?> handleBadRequest(RuntimeException ex) {
        return response(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public org.springframework.http.ResponseEntity<?> handleGeneric(Exception ex) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
    }

    private org.springframework.http.ResponseEntity<?> response(HttpStatus status, String message) {
        return org.springframework.http.ResponseEntity.status(status)
                .body(Map.of("timestamp", Instant.now(), "status", status.value(), "error", message));
    }
}
