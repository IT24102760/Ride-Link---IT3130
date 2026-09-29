package com.ridelink.farepaymentservice.model;

// CARD and MOBILE are simulated instantly; CASH is confirmed by the assigned driver
public enum PaymentMethod {
    CARD,
    MOBILE,
    CASH
}
