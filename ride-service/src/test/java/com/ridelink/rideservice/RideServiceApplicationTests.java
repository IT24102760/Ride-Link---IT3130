package com.ridelink.rideservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Checks the app starts; uses dummy test-only secrets (never real ones)
@SpringBootTest(properties = {
        "app.jwt.secret=test-only-secret-that-is-at-least-32-characters",
        "app.internal.service-key=test-only-service-key"
})
class RideServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}