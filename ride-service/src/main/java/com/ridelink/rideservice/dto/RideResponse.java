package com.ridelink.rideservice.dto;

import com.ridelink.rideservice.model.Location;
import com.ridelink.rideservice.model.Ride;
import com.ridelink.rideservice.model.RideStatus;
import com.ridelink.rideservice.model.VehicleType;

import java.math.BigDecimal;
import java.time.Instant;

// What our API returns for a ride
public record RideResponse(
        String rideId, String passengerId, String driverId, VehicleType vehicleType,
        Location pickup, Location destination, RideStatus status,
        BigDecimal finalFare, String paymentId, String cancelReason,
        Instant requestedAt, Instant startedAt, Instant completedAt) {

    // Converts a stored Ride into a response
    public static RideResponse from(Ride r) {
        return new RideResponse(
                r.getId(), r.getPassengerId(), r.getDriverId(), r.getVehicleType(),
                r.getPickup(), r.getDestination(), r.getStatus(),
                r.getFinalFare(), r.getPaymentId(), r.getCancelReason(),
                r.getRequestedAt(), r.getStartedAt(), r.getCompletedAt());
    }
}