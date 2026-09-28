package com.ridelink.farepaymentservice.dto;

import com.ridelink.farepaymentservice.model.PaymentMethod;
import com.ridelink.farepaymentservice.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReceiptResponse(

        String paymentId,

        String rideId,

        String passengerId,

        String driverId,

        BigDecimal finalFare,

        PaymentMethod paymentMethod,

        PaymentStatus status,

        String transactionReference,

        String receiptNumber,

        LocalDateTime paidAt

) {
}
