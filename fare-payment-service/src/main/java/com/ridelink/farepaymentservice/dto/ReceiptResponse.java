package com.ridelink.farepaymentservice.dto;

import com.ridelink.farepaymentservice.model.FareBreakdown;
import com.ridelink.farepaymentservice.model.Payment;
import com.ridelink.farepaymentservice.model.PaymentMethod;
import com.ridelink.farepaymentservice.model.VehicleType;

import java.math.BigDecimal;
import java.time.Instant;

// The receipt for a PAID payment
public record ReceiptResponse(
        String receiptNumber, String transactionReference, String paymentId, String rideId,
        VehicleType vehicleType, String pickupPlace, String destinationPlace,
        FareBreakdown fareBreakdown, BigDecimal totalPaid, PaymentMethod paymentMethod, Instant paidAt) {

    public static ReceiptResponse from(Payment p) {
        return new ReceiptResponse(p.getReceiptNumber(), p.getTransactionReference(), p.getId(), p.getRideId(),
                p.getVehicleType(), p.getPickupPlace(), p.getDestinationPlace(),
                p.getFareBreakdown(), p.getFinalFare(), p.getPaymentMethod(), p.getPaidAt());
    }
}
