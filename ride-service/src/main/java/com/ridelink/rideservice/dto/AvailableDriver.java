package com.ridelink.rideservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// One driver returned by GET /api/drivers/available (we only need these two ids)
@JsonIgnoreProperties(ignoreUnknown = true)
public record AvailableDriver(String driverId, String accountId) {
}