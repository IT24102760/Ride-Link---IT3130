package com.ridelink.accountservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Checks the app starts; uses dummy test-only values (never real secrets) and no admin seeding
@SpringBootTest(properties = {
        "app.jwt.secret=test-only-secret-that-is-at-least-32-characters",
        "app.admin.email=",
        "app.admin.password="
})
class AccountServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
