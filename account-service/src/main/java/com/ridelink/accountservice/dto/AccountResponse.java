package com.ridelink.accountservice.dto;

import com.ridelink.accountservice.model.Account;
import com.ridelink.accountservice.model.AccountStatus;
import com.ridelink.accountservice.model.Role;

import java.time.Instant;

// What our API returns for an account (never the password)
public record AccountResponse(String id, String fullName, String email, String phone,
                              Role role, AccountStatus status, Instant createdAt) {

    // Converts a stored Account into a response
    public static AccountResponse from(Account a) {
        return new AccountResponse(a.getId(), a.getFullName(), a.getEmail(), a.getPhone(),
                a.getRole(), a.getStatus(), a.getCreatedAt());
    }
}
