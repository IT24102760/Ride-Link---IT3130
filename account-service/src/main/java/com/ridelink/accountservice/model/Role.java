package com.ridelink.accountservice.model;

// What a user is allowed to do in RideLink (sent to other services in the token's "roles" claim)
public enum Role {
    PASSENGER,
    DRIVER,
    ADMIN
}
