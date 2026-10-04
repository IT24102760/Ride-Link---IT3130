package com.ridelink.farepaymentservice.dto;

import java.math.BigDecimal;

public record FareBreakdownRequest(

        BigDecimal baseFare,

        BigDecimal distanceCharge,

        BigDecimal timeCharge,

        BigDecimal minimumFare,

        boolean minimumApplied,

        BigDecimal totalFare

) {
}
