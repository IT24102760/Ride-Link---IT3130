package com.ridelink.farepaymentservice.service;

import com.ridelink.farepaymentservice.model.VehicleType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FareRule {

    public BigDecimal getBaseFare(VehicleType vehicleType) {
        return switch (vehicleType) {
            case BIKE -> new BigDecimal("100.00");
            case TUK -> new BigDecimal("150.00");
            case CAR -> new BigDecimal("200.00");
        };
    }

    public BigDecimal getPerKmRate(VehicleType vehicleType) {
        return switch (vehicleType) {
            case BIKE -> new BigDecimal("40.00");
            case TUK -> new BigDecimal("50.00");
            case CAR -> new BigDecimal("70.00");
        };
    }

    public BigDecimal getPerMinuteRate(VehicleType vehicleType) {
        return switch (vehicleType) {
            case BIKE -> new BigDecimal("2.00");
            case TUK -> new BigDecimal("3.00");
            case CAR -> new BigDecimal("4.00");
        };
    }

    public BigDecimal getMinimumFare(VehicleType vehicleType) {
        return switch (vehicleType) {
            case BIKE -> new BigDecimal("120.00");
            case TUK -> new BigDecimal("180.00");
            case CAR -> new BigDecimal("250.00");
        };
    }
}
