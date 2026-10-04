package com.ridelink.farepaymentservice.controller;

import com.ridelink.farepaymentservice.dto.PayRequest;
import com.ridelink.farepaymentservice.dto.PaymentResponse;
import com.ridelink.farepaymentservice.dto.ReceiptResponse;
import com.ridelink.farepaymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Payment endpoints. The caller's id and role always come from their token.
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ===================== 2. Passenger =====================

    @Tag(name = "2. Passenger")
    @Operation(summary = "Pay for a completed ride",
            description = "CARD/MOBILE: PAID, or FAILED if simulateOutcome is FAIL (you can pay again). "
                    + "CASH: AWAITING_DRIVER_CONFIRMATION until the driver confirms.")
    @ApiResponse(responseCode = "200", description = "Payment processed")
    @ApiResponse(responseCode = "403", description = "Not the passenger of this ride")
    @ApiResponse(responseCode = "404", description = "Payment not found")
    @ApiResponse(responseCode = "409", description = "Already PAID, or waiting for the driver's cash confirmation")
    @PreAuthorize("hasRole('PASSENGER')")
    @PostMapping("/{paymentId}/pay")
    public PaymentResponse pay(@AuthenticationPrincipal Jwt jwt, @PathVariable String paymentId,
                               @Valid @RequestBody PayRequest request) {
        return paymentService.pay(paymentId, jwt.getSubject(), request);
    }

    // ===================== 3. Driver =====================

    @Tag(name = "3. Driver")
    @Operation(summary = "Confirm the cash was received",
            description = "Only the driver of the ride, and only while the payment is AWAITING_DRIVER_CONFIRMATION.")
    @ApiResponse(responseCode = "200", description = "Payment PAID")
    @ApiResponse(responseCode = "403", description = "Not the driver of this ride")
    @ApiResponse(responseCode = "409", description = "No cash payment waiting for confirmation")
    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping("/{paymentId}/confirm-cash")
    public PaymentResponse confirmCash(@AuthenticationPrincipal Jwt jwt, @PathVariable String paymentId) {
        return paymentService.confirmCash(paymentId, jwt.getSubject());
    }

    // ===================== 4. Payments and receipts =====================

    @Tag(name = "4. Payments and receipts")
    @Operation(summary = "My payments (passenger: paid by me, driver: paid to me)")
    @GetMapping("/me")
    public List<PaymentResponse> myPayments(@AuthenticationPrincipal Jwt jwt) {
        return paymentService.getMyPayments(jwt.getSubject(), role(jwt));
    }

    @Tag(name = "4. Payments and receipts")
    @Operation(summary = "View one payment and its status")
    @ApiResponse(responseCode = "403", description = "Not part of this payment")
    @ApiResponse(responseCode = "404", description = "Payment not found")
    @GetMapping("/{paymentId}")
    public PaymentResponse getPayment(@AuthenticationPrincipal Jwt jwt, @PathVariable String paymentId) {
        return paymentService.getPayment(paymentId, jwt.getSubject(), role(jwt));
    }

    @Tag(name = "4. Payments and receipts")
    @Operation(summary = "View the payment of a ride")
    @ApiResponse(responseCode = "404", description = "No payment for this ride yet")
    @GetMapping("/ride/{rideId}")
    public PaymentResponse getPaymentForRide(@AuthenticationPrincipal Jwt jwt, @PathVariable String rideId) {
        return paymentService.getPaymentForRide(rideId, jwt.getSubject(), role(jwt));
    }

    @Tag(name = "4. Payments and receipts")
    @Operation(summary = "Get the receipt of a PAID payment")
    @ApiResponse(responseCode = "403", description = "Not part of this payment")
    @ApiResponse(responseCode = "409", description = "Payment is not PAID yet")
    @GetMapping("/{paymentId}/receipt")
    public ReceiptResponse getReceipt(@AuthenticationPrincipal Jwt jwt, @PathVariable String paymentId) {
        return paymentService.getReceipt(paymentId, jwt.getSubject(), role(jwt));
    }

    // First role in the token, e.g. "PASSENGER"
    private String role(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles == null || roles.isEmpty() ? "" : roles.get(0);
    }
}
