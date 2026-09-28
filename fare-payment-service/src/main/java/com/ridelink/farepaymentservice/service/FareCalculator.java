package com.ridelink.farepaymentservice.service;

import com.ridelink.farepaymentservice.dto.Location;
import com.ridelink.farepaymentservice.model.FareBreakdown;
import com.ridelink.farepaymentservice.model.VehicleType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FareCalculator {

    private final FareRule fareRule;
    private final DistanceCalculator distanceCalculator;

    public FareCalculator(
            FareRule fareRule,
            DistanceCalculator distanceCalculator) {

        this.fareRule = fareRule;
        this.distanceCalculator = distanceCalculator;
    }

    public FareBreakdown calculate(
            VehicleType vehicleType,
            Location pickup,
            Location destination,
            long tripMinutes) {

        BigDecimal baseFare =
                fareRule.getBaseFare(vehicleType);

        BigDecimal perKmRate =
                fareRule.getPerKmRate(vehicleType);

        BigDecimal perMinuteRate =
                fareRule.getPerMinuteRate(vehicleType);

        BigDecimal minimumFare =
                fareRule.getMinimumFare(vehicleType);

        BigDecimal distanceKm =
                distanceCalculator.calculateDistance(
                        pickup,
                        destination
                );

        BigDecimal distanceCharge =
                distanceKm
                        .multiply(perKmRate)
                        .setScale(2, RoundingMode.HALF_UP);

        BigDecimal timeCharge =
                perMinuteRate
                        .multiply(BigDecimal.valueOf(tripMinutes))
                        .setScale(2, RoundingMode.HALF_UP);

        BigDecimal calculatedFare =
                baseFare
                        .add(distanceCharge)
                        .add(timeCharge)
                        .setScale(2, RoundingMode.HALF_UP);

        boolean minimumApplied =
                calculatedFare.compareTo(minimumFare) < 0;

        BigDecimal totalFare;

        if (minimumApplied) {
            totalFare = minimumFare;
        } else {
            totalFare = calculatedFare;
        }

        return new FareBreakdown(
                baseFare,
                distanceCharge,
                timeCharge,
                minimumFare,
                minimumApplied,
                totalFare
        );
    }

    public BigDecimal calculateDistance(
            Location pickup,
            Location destination) {

        return distanceCalculator.calculateDistance(
                pickup,
                destination
        );
    }
}
