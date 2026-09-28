package com.ridelink.farepaymentservice.dto;

import com.ridelink.farepaymentservice.model.PaymentStatus;

import java.math.BigDecimal;

public record PaymentResponse(

        String paymentId,

        BigDecimal finalFare,

        PaymentStatus status

) {
}
