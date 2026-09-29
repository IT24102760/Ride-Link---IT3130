package com.ridelink.farepaymentservice.controller;

import com.ridelink.farepaymentservice.config.ServiceKeyValidator;
import com.ridelink.farepaymentservice.dto.CreatePaymentRequest;
import com.ridelink.farepaymentservice.dto.PaymentResponse;
import com.ridelink.farepaymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Called only by the Ride service, protected by the shared X-Service-Key (no user token)
@RestController
@RequestMapping("/internal/payments")
@Tag(name = "5. Internal")
@SecurityRequirements   // no JWT lock in Swagger: uses the service key instead
public class InternalPaymentController {

    private final PaymentService paymentService;
    private final ServiceKeyValidator serviceKeyValidator;

    public InternalPaymentController(PaymentService paymentService, ServiceKeyValidator serviceKeyValidator) {
        this.paymentService = paymentService;
        this.serviceKeyValidator = serviceKeyValidator;
    }

    @Operation(summary = "Create the PENDING payment for a completed ride (Ride service only)",
            description = "Calculates the final fare from the place names, vehicle type and trip minutes. "
                    + "Calling it again for the same ride returns the existing payment.")
    @ApiResponse(responseCode = "201", description = "Payment created (PENDING), or the existing one")
    @ApiResponse(responseCode = "400", description = "Invalid input or unknown place")
    @ApiResponse(responseCode = "401", description = "Missing or invalid service key")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(
            @Parameter(description = "Shared service key") @RequestHeader(value = "X-Service-Key", required = false) String serviceKey,
            @Valid @RequestBody CreatePaymentRequest request) {
        serviceKeyValidator.verify(serviceKey);   // 401 before anything else
        return paymentService.createPayment(request);
    }
}
