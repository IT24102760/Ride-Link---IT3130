package com.ridelink.rideservice.controller;

import com.ridelink.rideservice.dto.AssignDriverRequest;
import com.ridelink.rideservice.dto.AvailableDriver;
import com.ridelink.rideservice.dto.CancelRideRequest;
import com.ridelink.rideservice.dto.CreateRideRequest;
import com.ridelink.rideservice.dto.RideResponse;
import com.ridelink.rideservice.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Ride endpoints, grouped in Swagger by who uses them.
// The caller's id and role always come from their JWT token.
@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    // ===================== 1. Passenger =====================

    @Tag(name = "1. Passenger")
    @Operation(summary = "Request a new ride",
            description = "Step 1. The passenger asks for a ride. The ride is saved as REQUESTED. "
                    + "Copy the rideId from the response for the next steps.")
    @ApiResponse(responseCode = "201", description = "Ride created (REQUESTED)")
    @ApiResponse(responseCode = "400", description = "Invalid input, e.g. empty place name")
    @ApiResponse(responseCode = "403", description = "Only passengers can request rides")
    @PreAuthorize("hasRole('PASSENGER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RideResponse requestRide(@AuthenticationPrincipal Jwt jwt,
                                    @Valid @RequestBody CreateRideRequest request) {
        return rideService.requestRide(jwt.getSubject(), request);
    }

    @Tag(name = "1. Passenger")
    @Operation(summary = "See the available drivers for my ride",
            description = "Optional step 2a. The Ride service asks the Driver service for the ONLINE drivers "
                    + "in the pickup area with the right vehicle type, longest-waiting first. "
                    + "Pick one and send its driverId to the assign step.")
    @ApiResponse(responseCode = "200", description = "The available drivers (may be empty)")
    @ApiResponse(responseCode = "403", description = "Only the passenger who requested this ride can see its drivers")
    @ApiResponse(responseCode = "409", description = "The ride is not REQUESTED")
    @ApiResponse(responseCode = "503", description = "Driver service is unavailable")
    @PreAuthorize("hasAnyRole('PASSENGER','ADMIN')")
    @GetMapping("/{rideId}/drivers")
    public List<AvailableDriver> availableDrivers(@AuthenticationPrincipal Jwt jwt,
                                                  @Parameter(description = "The ride's id") @PathVariable String rideId,
                                                  @Parameter(hidden = true)
                                                  @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        // the passenger's token is forwarded to the Driver service
        return rideService.findDriversForRide(rideId, jwt.getSubject(), role(jwt), authorization);
    }

    @Tag(name = "1. Passenger")
    @Operation(summary = "Assign a driver (my choice, or the longest-waiting one)",
            description = "Step 2. Send a body with a driverId (from the drivers list) to choose that driver, "
                    + "or send no body to get the longest-waiting AVAILABLE driver in the pickup area. "
                    + "The driver is marked BUSY and assigned to the ride.")
    @ApiResponse(responseCode = "200", description = "Driver assigned (ASSIGNED)")
    @ApiResponse(responseCode = "403", description = "Only the passenger who requested this ride can assign a driver")
    @ApiResponse(responseCode = "409",
            description = "No available driver, the chosen driver is not available, or the ride is not REQUESTED")
    @ApiResponse(responseCode = "503", description = "Driver service is unavailable")
    @PreAuthorize("hasAnyRole('PASSENGER','ADMIN')")
    @PostMapping("/{rideId}/assign")
    public RideResponse assignDriver(@AuthenticationPrincipal Jwt jwt,
                                     @Parameter(description = "The ride's id") @PathVariable String rideId,
                                     @org.springframework.web.bind.annotation.RequestBody(required = false)
                                     AssignDriverRequest request,
                                     @Parameter(hidden = true)
                                     @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        // the passenger's token is forwarded to the Driver service
        return rideService.assignDriver(rideId, jwt.getSubject(), role(jwt), authorization,
                request == null ? null : request.driverId());
    }

    // ===================== 2. Driver =====================

    @Tag(name = "2. Driver")
    @Operation(summary = "Accept the assigned ride",
            description = "Step 3. The assigned driver agrees to take the ride.")
    @ApiResponse(responseCode = "200", description = "Ride accepted (ACCEPTED)")
    @ApiResponse(responseCode = "403", description = "Only the assigned driver can accept")
    @ApiResponse(responseCode = "409", description = "The ride is not ASSIGNED")
    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping("/{rideId}/accept")
    public RideResponse acceptRide(@AuthenticationPrincipal Jwt jwt,
                                   @Parameter(description = "The ride's id") @PathVariable String rideId) {
        return rideService.acceptRide(rideId, jwt.getSubject());
    }

    @Tag(name = "2. Driver")
    @Operation(summary = "Start the trip",
            description = "Step 4. The driver has picked up the passenger. The start time is saved "
                    + "and used later to calculate the trip time.")
    @ApiResponse(responseCode = "200", description = "Trip started (IN_PROGRESS)")
    @ApiResponse(responseCode = "403", description = "Only the assigned driver can start the trip")
    @ApiResponse(responseCode = "409", description = "The ride is not ACCEPTED")
    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping("/{rideId}/start")
    public RideResponse startRide(@AuthenticationPrincipal Jwt jwt,
                                  @Parameter(description = "The ride's id") @PathVariable String rideId) {
        return rideService.startRide(rideId, jwt.getSubject());
    }

    @Tag(name = "2. Driver")
    @Operation(summary = "Complete the trip and create the payment",
            description = "Step 5. The Ride service sends the trip details to the Fare service, which "
                    + "calculates the final fare and creates a PENDING payment. The driver becomes AVAILABLE again.")
    @ApiResponse(responseCode = "200", description = "Trip completed (COMPLETED), with finalFare and paymentId")
    @ApiResponse(responseCode = "403", description = "Only the assigned driver can complete the trip")
    @ApiResponse(responseCode = "409", description = "The ride is not IN_PROGRESS")
    @ApiResponse(responseCode = "503", description = "Fare service is unavailable; the ride stays IN_PROGRESS")
    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping("/{rideId}/complete")
    public RideResponse completeRide(@AuthenticationPrincipal Jwt jwt,
                                     @Parameter(description = "The ride's id") @PathVariable String rideId) {
        return rideService.completeRide(rideId, jwt.getSubject());
    }

    // ===================== 3. Passenger & Driver =====================

    @Tag(name = "3. Passenger & Driver")
    @Operation(summary = "Cancel a ride before the trip starts",
            description = "The ride's passenger, its assigned driver or an admin can cancel from "
                    + "REQUESTED, ASSIGNED or ACCEPTED. An assigned driver becomes AVAILABLE again.")
    @ApiResponse(responseCode = "200", description = "Ride cancelled (CANCELLED)")
    @ApiResponse(responseCode = "403", description = "You are not part of this ride")
    @ApiResponse(responseCode = "409", description = "The trip already started, finished or was cancelled")
    @PostMapping("/{rideId}/cancel")
    public RideResponse cancelRide(@AuthenticationPrincipal Jwt jwt,
                                   @Parameter(description = "The ride's id") @PathVariable String rideId,
                                   @Valid @RequestBody CancelRideRequest request) {
        return rideService.cancelRide(rideId, jwt.getSubject(), role(jwt), request.reason());
    }

    @Tag(name = "3. Passenger & Driver")
    @Operation(summary = "View one ride",
            description = "The ride's passenger, its assigned driver or an admin can view it.")
    @ApiResponse(responseCode = "200", description = "The ride")
    @ApiResponse(responseCode = "403", description = "You are not part of this ride")
    @ApiResponse(responseCode = "404", description = "Ride not found")
    @GetMapping("/{rideId}")
    public RideResponse getRide(@AuthenticationPrincipal Jwt jwt,
                                @Parameter(description = "The ride's id") @PathVariable String rideId) {
        return rideService.getRide(rideId, jwt.getSubject(), role(jwt));
    }

    @Tag(name = "3. Passenger & Driver")
    @Operation(summary = "List my rides",
            description = "A passenger sees the rides they booked; a driver sees the rides assigned to them.")
    @ApiResponse(responseCode = "200", description = "The caller's rides")
    @GetMapping("/me")
    public List<RideResponse> myRides(@AuthenticationPrincipal Jwt jwt) {
        return rideService.getMyRides(jwt.getSubject(), role(jwt));
    }

    // First role in the token, e.g. "PASSENGER"
    private String role(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles == null || roles.isEmpty() ? "" : roles.get(0);
    }
}