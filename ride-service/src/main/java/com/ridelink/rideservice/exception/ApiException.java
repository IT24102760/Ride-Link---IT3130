package com.ridelink.rideservice.exception;

import org.springframework.http.HttpStatus;

// Thrown by our code when a request breaks a rule (e.g. 404, 403, 409)
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}