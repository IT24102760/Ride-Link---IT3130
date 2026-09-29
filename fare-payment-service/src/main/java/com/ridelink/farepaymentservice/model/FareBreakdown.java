package com.ridelink.farepaymentservice.model;

import java.math.BigDecimal;

// How a fare was calculated (returned by the estimate and stored with each payment)
public record FareBreakdown(
        BigDecimal distanceKm,
        long tripMinutes,
        BigDecimal baseFare,
        BigDecimal distanceCharge,
        BigDecimal timeCharge,
        BigDecimal minimumFare,
        boolean minimumApplied,
        BigDecimal totalFare) {
}

