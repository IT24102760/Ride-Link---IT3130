package com.ridelink.rideservice.exception;

import java.time.Instant;
import java.util.Map;

// The JSON body returned for every error (same shape in all services)
public record ApiError(Instant timestamp, int status, String error, String message,
                       String path, Map<String, String> fieldErrors) {
}