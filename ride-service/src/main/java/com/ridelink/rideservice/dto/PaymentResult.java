package com.ridelink.rideservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

// What the Fare service returns after creating a payment
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentResult(String paymentId, BigDecimal finalFare, String status) {
}