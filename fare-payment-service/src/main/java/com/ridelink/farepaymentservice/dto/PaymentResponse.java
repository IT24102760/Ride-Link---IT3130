package com.ridelink.farepaymentservice.dto;

import com.ridelink.farepaymentservice.model.*;

import java.math.BigDecimal;
import java.time.Instant;

// A payment as returned by our API. The Ride service reads paymentId, finalFare and status.
public record PaymentResponse(
        String paymentId, String rideId, String passengerId, String driverId,
        VehicleType vehicleType, String pickupPlace, String destinationPlace,
        FareBreakdown fareBreakdown, BigDecimal finalFare,
        PaymentStatus status, PaymentMethod paymentMethod,
        Instant createdAt, Instant paidAt) {

    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.getId(), p.getRideId(), p.getPassengerId(), p.getDriverId(),
                p.getVehicleType(), p.getPickupPlace(), p.getDestinationPlace(),
                p.getFareBreakdown(), p.getFinalFare(),
                p.getStatus(), p.getPaymentMethod(), p.getCreatedAt(), p.getPaidAt());
    }
}
