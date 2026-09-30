package com.ridelink.accountservice.service;

import com.ridelink.accountservice.model.Account;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

// Creates the signed login token. The claims below are the contract every other service relies on:
//   sub   = account id   (Ride uses it as passengerId / driverAccountId)
//   roles = ["PASSENGER"] / ["DRIVER"] / ["ADMIN"]
@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long expiryMinutes;

    public JwtService(JwtEncoder jwtEncoder, @Value("${app.jwt.expiry-minutes:120}") long expiryMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.expiryMinutes = expiryMinutes;
    }

    public String createToken(Account account) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(account.getId())
                .claim("roles", List.of(account.getRole().name()))
                .claim("email", account.getEmail())
                .claim("fullName", account.getFullName())
                .issuedAt(now)
                .expiresAt(now.plus(expiryMinutes, ChronoUnit.MINUTES))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    // Token lifetime in seconds (returned to the client as expiresIn)
    public long getExpirySeconds() {
        return expiryMinutes * 60;
    }
}
