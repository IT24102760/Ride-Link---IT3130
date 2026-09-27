package com.ridelink.rideservice.controller;

import com.ridelink.rideservice.dto.CancelRideRequest;
import com.ridelink.rideservice.dto.CreateRideRequest;
import com.ridelink.rideservice.dto.RideResponse;
import com.ridelink.rideservice.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Ride endpoints. The caller's id and role come from their JWT token.
@RestController
@RequestMapping("/api/rides")
@Tag(name = "Rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @Operation(summary = "Request a new ride")
    @PreAuthorize("hasRole('PASSENGER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)  // returns 201
    public RideResponse requestRide(@AuthenticationPrincipal Jwt jwt,
                                    @Valid @RequestBody CreateRideRequest request) {
        return rideService.requestRide(jwt.getSubject(), request);
    }

    @Operation(summary = "Assigned driver accepts the ride")
    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping("/{rideId}/accept")
    public RideResponse acceptRide(@AuthenticationPrincipal Jwt jwt, @PathVariable String rideId) {
        return rideService.acceptRide(rideId, jwt.getSubject());
    }

    @Operation(summary = "Assigned driver starts the trip")
    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping("/{rideId}/start")
    public RideResponse startRide(@AuthenticationPrincipal Jwt jwt, @PathVariable String rideId) {
        return rideService.startRide(rideId, jwt.getSubject());
    }

    @Operation(summary = "Cancel a ride before the trip starts")
    @PostMapping("/{rideId}/cancel")
    public RideResponse cancelRide(@AuthenticationPrincipal Jwt jwt, @PathVariable String rideId,
                                   @Valid @RequestBody CancelRideRequest request) {
        return rideService.cancelRide(rideId, jwt.getSubject(), role(jwt), request.reason());
    }

    @Operation(summary = "List my rides")
    @GetMapping("/me")
    public List<RideResponse> myRides(@AuthenticationPrincipal Jwt jwt) {
        return rideService.getMyRides(jwt.getSubject(), role(jwt));
    }

    @Operation(summary = "View one ride")
    @GetMapping("/{rideId}")
    public RideResponse getRide(@AuthenticationPrincipal Jwt jwt, @PathVariable String rideId) {
        return rideService.getRide(rideId, jwt.getSubject(), role(jwt));
    }

    // First role in the token, e.g. "PASSENGER"
    private String role(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles == null || roles.isEmpty() ? "" : roles.get(0);
    }
}