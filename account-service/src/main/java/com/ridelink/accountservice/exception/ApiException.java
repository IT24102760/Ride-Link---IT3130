package com.ridelink.accountservice.exception;

import org.springframework.http.HttpStatus;

// Thrown by our code when a request breaks a rule (e.g. 401, 403, 404, 409)
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
