package com.ridelink.farepaymentservice.service;

import com.ridelink.farepaymentservice.model.FareBreakdown;
import com.ridelink.farepaymentservice.model.VehicleType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Applies the documented FareRule to two place names
@Service
public class FareCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final PlaceDirectory placeDirectory;

    public FareCalculator(PlaceDirectory placeDirectory) {
        this.placeDirectory = placeDirectory;
    }

    // Final fare, with the real trip time sent by the Ride service
    public FareBreakdown calculate(VehicleType vehicleType, String pickup, String destination, long tripMinutes) {
        BigDecimal distanceKm = distanceKm(pickup, destination);
        FareRule rule = FareRule.forVehicle(vehicleType);

        BigDecimal distanceCharge = money(distanceKm.multiply(rule.perKm()));
        BigDecimal timeCharge = money(rule.perMinute().multiply(BigDecimal.valueOf(tripMinutes)));
        BigDecimal calculated = money(rule.baseFare().add(distanceCharge).add(timeCharge));

        boolean minimumApplied = calculated.compareTo(rule.minimumFare()) < 0;
        BigDecimal total = minimumApplied ? money(rule.minimumFare()) : calculated;

        return new FareBreakdown(distanceKm, tripMinutes, money(rule.baseFare()), distanceCharge, timeCharge,
                money(rule.minimumFare()), minimumApplied, total);
    }

    // Estimate before booking: the trip time is guessed from the distance at an average speed
    public FareBreakdown estimate(VehicleType vehicleType, String pickup, String destination) {
        double km = distanceKm(pickup, destination).doubleValue();
        long estimatedMinutes = Math.max(1, Math.round(km / FareRule.AVERAGE_SPEED_KMH * 60));
        return calculate(vehicleType, pickup, destination, estimatedMinutes);
    }

    // Straight-line (Haversine) distance between the two places x road factor
    BigDecimal distanceKm(String pickup, String destination) {
        PlaceDirectory.Place from = placeDirectory.find(pickup);
        PlaceDirectory.Place to = placeDirectory.find(destination);

        double dLat = Math.toRadians(to.latitude() - from.latitude());
        double dLon = Math.toRadians(to.longitude() - from.longitude());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(from.latitude())) * Math.cos(Math.toRadians(to.latitude()))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double straightLineKm = EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return BigDecimal.valueOf(straightLineKm * FareRule.ROAD_FACTOR).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}

