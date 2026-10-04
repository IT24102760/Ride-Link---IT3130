package com.ridelink.farepaymentservice.config;

import com.ridelink.farepaymentservice.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

// Checks the X-Service-Key header on /internal/** calls from the Ride service
@Component
public class ServiceKeyValidator {

    private final String expectedKey;

    public ServiceKeyValidator(@Value("${app.internal.service-key}") String expectedKey) {
        this.expectedKey = expectedKey;
    }

    public void verify(String providedKey) {
        // constant-time comparison, so the key can't be guessed from response timing
        boolean ok = providedKey != null && MessageDigest.isEqual(
                providedKey.getBytes(StandardCharsets.UTF_8), expectedKey.getBytes(StandardCharsets.UTF_8));
        if (!ok) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Missing or invalid service key");
        }
    }
}
