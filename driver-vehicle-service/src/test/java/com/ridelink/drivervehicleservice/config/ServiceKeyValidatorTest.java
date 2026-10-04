package com.ridelink.drivervehicleservice.config;

import com.ridelink.drivervehicleservice.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

// The Ride service must send the right X-Service-Key for /internal/** calls
class ServiceKeyValidatorTest {

    private final ServiceKeyValidator validator = new ServiceKeyValidator("secret-key");

    @Test
    void correctKey_isAccepted() {
        assertDoesNotThrow(() -> validator.verify("secret-key"));
    }

    @Test
    void wrongKey_isUnauthorized() {
        ApiException ex = assertThrows(ApiException.class, () -> validator.verify("wrong-key"));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void missingKey_isUnauthorized() {
        ApiException ex = assertThrows(ApiException.class, () -> validator.verify(null));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }
}
