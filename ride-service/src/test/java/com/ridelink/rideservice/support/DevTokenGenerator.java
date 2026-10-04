package com.ridelink.rideservice.support;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

// DEV ONLY: prints test tokens signed with JWT_SECRET, until the Account service is ready
public class DevTokenGenerator {

    public static void main(String[] args) {
        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("Set JWT_SECRET (from local.env) in this run configuration");
        }
        var key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));

        System.out.println("PASSENGER token:\n" + token(encoder, "passenger-1", "PASSENGER"));
        System.out.println("\nDRIVER token:\n" + token(encoder, "driver-1", "DRIVER"));
    }

    // Builds one signed token for the given user id and role
    private static String token(JwtEncoder encoder, String accountId, String role) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(now.plus(2, ChronoUnit.HOURS))  // valid for 2 hours
                .subject(accountId)                        // the user's id (jwt.getSubject())
                .claim("roles", List.of(role))             // the user's role
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}