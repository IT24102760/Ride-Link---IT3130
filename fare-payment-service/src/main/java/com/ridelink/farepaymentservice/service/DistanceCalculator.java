package com.ridelink.farepaymentservice.service;

import com.ridelink.farepaymentservice.dto.Location;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class DistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    public BigDecimal calculateDistance(
            Location pickup,
            Location destination) {

        double pickupLatitude = Math.toRadians(pickup.latitude());
        double pickupLongitude = Math.toRadians(pickup.longitude());

        double destinationLatitude =
                Math.toRadians(destination.latitude());

        double destinationLongitude =
                Math.toRadians(destination.longitude());

        double latitudeDifference =
                destinationLatitude - pickupLatitude;

        double longitudeDifference =
                destinationLongitude - pickupLongitude;

        double a =
                Math.sin(latitudeDifference / 2)
                        * Math.sin(latitudeDifference / 2)
                        + Math.cos(pickupLatitude)
                        * Math.cos(destinationLatitude)
                        * Math.sin(longitudeDifference / 2)
                        * Math.sin(longitudeDifference / 2);

        double c =
                2 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a)
                );

        double distanceKm = EARTH_RADIUS_KM * c;

        return BigDecimal.valueOf(distanceKm)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
