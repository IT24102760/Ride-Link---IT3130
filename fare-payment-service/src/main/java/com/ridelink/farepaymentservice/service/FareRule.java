package com.ridelink.farepaymentservice.service;

import com.ridelink.farepaymentservice.model.VehicleType;

import java.math.BigDecimal;

// DOCUMENTED FARE RULE (all amounts in LKR)
//   distance = straight-line distance between the two places x 1.3 (road factor)
//   fare     = base fare + (distance x per-km rate) + (trip minutes x per-minute rate)
//   the fare is never lower than the minimum fare, and is rounded to 2 decimals
//
//             base    per km   per minute   minimum
//   BIKE      100      40         2           120
//   TUK       150      50         3           180
//   CAR       200      70         4           250
public record FareRule(BigDecimal baseFare, BigDecimal perKm, BigDecimal perMinute, BigDecimal minimumFare) {

    public static final double ROAD_FACTOR = 1.3;          // roads are longer than a straight line
    public static final double AVERAGE_SPEED_KMH = 30.0;   // used to estimate trip minutes

    public static FareRule forVehicle(VehicleType type) {
        return switch (type) {
            case BIKE -> of("100", "40", "2", "120");
            case TUK  -> of("150", "50", "3", "180");
            case CAR  -> of("200", "70", "4", "250");
        };
    }

    private static FareRule of(String base, String perKm, String perMinute, String minimum) {
        return new FareRule(new BigDecimal(base), new BigDecimal(perKm),
                new BigDecimal(perMinute), new BigDecimal(minimum));
    }
}