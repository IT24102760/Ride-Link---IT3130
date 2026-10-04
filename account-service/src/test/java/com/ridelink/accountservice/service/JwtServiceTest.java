package com.ridelink.accountservice.service;

import com.ridelink.accountservice.config.JwtConfig;
import com.ridelink.accountservice.model.Account;
import com.ridelink.accountservice.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Checks the token has exactly the claims the Driver, Ride and Fare services expect
class JwtServiceTest {

    private static final String SECRET = "test-only-secret-that-is-at-least-32-characters";

    private final JwtConfig config = new JwtConfig();
    private final SecretKey key = config.jwtSecretKey(SECRET);
    private final JwtService jwtService = new JwtService(config.jwtEncoder(key), 120);
    private final JwtDecoder decoder = config.jwtDecoder(key);   // same decoder setup as the other services

    @Test
    void token_containsSubRolesAndExpiry() {
        Account kasun = new Account();
        kasun.setId("acc-2");
        kasun.setEmail("kasun@ridelink.test");
        kasun.setFullName("Kasun Silva");
        kasun.setRole(Role.DRIVER);

        Jwt jwt = decoder.decode(jwtService.createToken(kasun));

        assertEquals("acc-2", jwt.getSubject());                           // Ride uses this as the driver's id
        assertEquals(List.of("DRIVER"), jwt.getClaimAsStringList("roles")); // becomes ROLE_DRIVER
        assertEquals("kasun@ridelink.test", jwt.getClaimAsString("email"));
        assertEquals(Duration.ofMinutes(120), Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()));
    }

    @Test
    void shortSecret_isRejected() {
        assertThrows(IllegalStateException.class, () -> config.jwtSecretKey("too-short"));
    }
}
