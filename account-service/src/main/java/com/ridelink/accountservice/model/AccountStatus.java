package com.ridelink.accountservice.model;

// Only ACTIVE accounts can log in
public enum AccountStatus {
    ACTIVE,
    SUSPENDED,
    DEACTIVATED
}
