package com.ridelink.drivervehicleservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Checks the app starts; uses dummy test-only values (never real secrets)
@SpringBootTest(properties = {
        "app.jwt.secret=test-only-secret-that-is-at-least-32-characters",
        "app.internal.service-key=test-only-service-key"
})
class DriverVehicleServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
