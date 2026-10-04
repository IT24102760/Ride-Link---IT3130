package com.ridelink.drivervehicleservice.controller;

import com.ridelink.drivervehicleservice.dto.*;
import com.ridelink.drivervehicleservice.model.VehicleType;
import com.ridelink.drivervehicleservice.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Driver endpoints. The driver is always identified by their token (jwt.getSubject()).
@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    // ===================== 1. Driver =====================

    @Tag(name = "1. Driver")
    @Operation(summary = "Create my driver profile and vehicle",
            description = "Step 1. One profile per driver account. The profile starts OFFLINE.")
    @ApiResponse(responseCode = "201", description = "Profile created (OFFLINE)")
    @ApiResponse(responseCode = "400", description = "Invalid input, e.g. bad plate number")
    @ApiResponse(responseCode = "403", description = "Only drivers can create a profile")
    @ApiResponse(responseCode = "409", description = "Profile or plate number already exists")
    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping("/me")
    @ResponseStatus(HttpStatus.CREATED)
    public DriverResponse createProfile(@AuthenticationPrincipal Jwt jwt,
                                        @Valid @RequestBody CreateDriverRequest request) {
        return driverService.createProfile(jwt.getSubject(), jwt.getClaimAsString("fullName"), request);
    }

    @Tag(name = "1. Driver")
    @Operation(summary = "View my driver profile and availability")
    @ApiResponse(responseCode = "404", description = "Profile not created yet")
    @PreAuthorize("hasRole('DRIVER')")
    @GetMapping("/me")
    public DriverResponse getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        return driverService.getMyProfile(jwt.getSubject());
    }

    @Tag(name = "1. Driver")
    @Operation(summary = "Update my licence, service area and vehicle")
    @ApiResponse(responseCode = "409", description = "On a ride, or plate number already registered")
    @PreAuthorize("hasRole('DRIVER')")
    @PutMapping("/me")
    public DriverResponse updateMyProfile(@AuthenticationPrincipal Jwt jwt,
                                          @Valid @RequestBody CreateDriverRequest request) {
        return driverService.updateMyProfile(jwt.getSubject(), request);
    }

    @Tag(name = "1. Driver")
    @Operation(summary = "Update my simulated current location")
    @PreAuthorize("hasRole('DRIVER')")
    @PatchMapping("/me/location")
    public DriverResponse updateMyLocation(@AuthenticationPrincipal Jwt jwt,
                                           @Valid @RequestBody LocationRequest request) {
        return driverService.updateMyLocation(jwt.getSubject(), request.placeName());
    }

    @Tag(name = "1. Driver")
    @Operation(summary = "Go online (AVAILABLE) or offline (OFFLINE)",
            description = "Step 2. Only AVAILABLE drivers can be assigned rides. BUSY is set only by the Ride service.")
    @ApiResponse(responseCode = "200", description = "Availability changed")
    @ApiResponse(responseCode = "400", description = "BUSY cannot be set by the driver")
    @ApiResponse(responseCode = "409", description = "Currently on a ride")
    @PreAuthorize("hasRole('DRIVER')")
    @PatchMapping("/me/availability")
    public DriverResponse setMyAvailability(@AuthenticationPrincipal Jwt jwt,
                                            @Valid @RequestBody AvailabilityRequest request) {
        return driverService.setMyAvailability(jwt.getSubject(), request.status());
    }

    // ===================== 2. Available drivers =====================

    @Tag(name = "2. Available drivers")
    @Operation(summary = "Find AVAILABLE drivers in an area",
            description = "Used by the Ride service during assignment (it forwards the passenger's token). "
                    + "Returns an empty list when nobody is available.")
    @ApiResponse(responseCode = "200", description = "Matching drivers, longest-waiting first")
    @ApiResponse(responseCode = "400", description = "Missing area or invalid vehicleType")
    @GetMapping("/available")
    public List<AvailableDriverResponse> findAvailable(
            @Parameter(description = "Pickup place name", example = "Negombo") @RequestParam String area,
            @Parameter(description = "BIKE, TUK or CAR", example = "CAR") @RequestParam VehicleType vehicleType) {
        return driverService.findAvailable(area, vehicleType);
    }

    // ===================== 4. Admin =====================

    @Tag(name = "4. Admin")
    @Operation(summary = "List all drivers")
    @ApiResponse(responseCode = "403", description = "Admin only")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<DriverResponse> getAllDrivers() {
        return driverService.getAllDrivers();
    }
}
