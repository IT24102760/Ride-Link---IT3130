package com.ridelink.drivervehicleservice.controller;

import com.ridelink.drivervehicleservice.config.ServiceKeyValidator;
import com.ridelink.drivervehicleservice.dto.AvailabilityRequest;
import com.ridelink.drivervehicleservice.dto.DriverResponse;
import com.ridelink.drivervehicleservice.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

// Called only by the Ride service, protected by the shared X-Service-Key (no user token)
@RestController
@RequestMapping("/internal/drivers")
@Tag(name = "3. Internal")
@SecurityRequirements   // no JWT lock in Swagger: uses the service key instead
public class InternalDriverController {

    private final DriverService driverService;
    private final ServiceKeyValidator serviceKeyValidator;

    public InternalDriverController(DriverService driverService, ServiceKeyValidator serviceKeyValidator) {
        this.driverService = driverService;
        this.serviceKeyValidator = serviceKeyValidator;
    }

    @Operation(summary = "Set a driver BUSY or AVAILABLE (Ride service only)",
            description = "BUSY when the Ride service assigns the driver; AVAILABLE when the ride is completed or cancelled.")
    @ApiResponse(responseCode = "200", description = "Availability changed")
    @ApiResponse(responseCode = "401", description = "Missing or invalid service key")
    @ApiResponse(responseCode = "404", description = "Driver not found")
    @ApiResponse(responseCode = "409", description = "BUSY requested but the driver is not AVAILABLE")
    @PatchMapping("/{driverId}/availability")
    public DriverResponse setAvailability(
            @Parameter(description = "Shared service key") @RequestHeader(value = "X-Service-Key", required = false) String serviceKey,
            @PathVariable String driverId,
            @Valid @RequestBody AvailabilityRequest request) {
        serviceKeyValidator.verify(serviceKey);   // 401 before anything else
        return driverService.setAvailabilityInternal(driverId, request.status());
    }
}
