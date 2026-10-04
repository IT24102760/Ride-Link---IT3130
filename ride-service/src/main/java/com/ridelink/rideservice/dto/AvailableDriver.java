package com.ridelink.rideservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// One driver returned by GET /api/drivers/available (ids to assign, name and plate to show the passenger)
@JsonIgnoreProperties(ignoreUnknown = true)
public record AvailableDriver(String driverId, String accountId, String fullName, String plateNumber) {

    // Used when only the ids are known
    public AvailableDriver(String driverId, String accountId) {
        this(driverId, accountId, null, null);
    }
}
