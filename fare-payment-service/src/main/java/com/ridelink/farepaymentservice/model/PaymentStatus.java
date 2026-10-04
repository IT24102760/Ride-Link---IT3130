package com.ridelink.farepaymentservice.model;

// PENDING -> PAID (card/mobile success)
// PENDING -> FAILED -> pay again
// PENDING -> AWAITING_DRIVER_CONFIRMATION -> PAID (cash, confirmed by the driver)
public enum PaymentStatus {
    PENDING,
    AWAITING_DRIVER_CONFIRMATION,
    PAID,
    FAILED
}