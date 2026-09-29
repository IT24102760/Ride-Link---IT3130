package com.ridelink.farepaymentservice.service;

import com.ridelink.farepaymentservice.dto.CreatePaymentRequest;
import com.ridelink.farepaymentservice.dto.PayRequest;
import com.ridelink.farepaymentservice.dto.PaymentResponse;
import com.ridelink.farepaymentservice.dto.ReceiptResponse;
import com.ridelink.farepaymentservice.exception.ApiException;
import com.ridelink.farepaymentservice.model.*;
import com.ridelink.farepaymentservice.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// Business rules for payments: create (from Ride), pay, cash confirmation, receipts and history
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final FareCalculator fareCalculator;

    public PaymentService(PaymentRepository paymentRepository, FareCalculator fareCalculator) {
        this.paymentRepository = paymentRepository;
        this.fareCalculator = fareCalculator;
    }

    // Internal (Ride service): a completed ride -> final fare -> PENDING payment.
    // If this ride already has a payment, the same one is returned, so a retry never bills twice.
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        var existing = paymentRepository.findByRideId(request.rideId());
        if (existing.isPresent()) {
            return PaymentResponse.from(existing.get());
        }

        FareBreakdown fare = fareCalculator.calculate(request.vehicleType(),
                request.pickup().placeName(), request.destination().placeName(), request.tripMinutes());

        Payment payment = new Payment();
        payment.setRideId(request.rideId());
        payment.setPassengerId(request.passengerId());
        payment.setDriverId(request.driverId());
        payment.setDriverAccountId(request.driverAccountId());
        payment.setVehicleType(request.vehicleType());
        payment.setPickupPlace(request.pickup().placeName().trim());
        payment.setDestinationPlace(request.destination().placeName().trim());
        payment.setFareBreakdown(fare);
        payment.setFinalFare(fare.totalFare());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(Instant.now());
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    // Passenger pays. CARD/MOBILE are simulated now; CASH waits for the driver's confirmation.
    public PaymentResponse pay(String paymentId, String callerId, PayRequest request) {
        Payment payment = findPayment(paymentId);
        if (!callerId.equals(payment.getPassengerId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the passenger of this ride can pay");
        }
        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new ApiException(HttpStatus.CONFLICT, "This payment is already PAID");
        }
        if (payment.getStatus() == PaymentStatus.AWAITING_DRIVER_CONFIRMATION) {
            throw new ApiException(HttpStatus.CONFLICT, "Cash payment is waiting for the driver's confirmation");
        }
        // here the status is PENDING or FAILED (a failed payment can be tried again)

        payment.setPaymentMethod(request.method());
        if (request.method() == PaymentMethod.CASH) {
            payment.setStatus(PaymentStatus.AWAITING_DRIVER_CONFIRMATION);
        } else if ("FAIL".equalsIgnoreCase(request.simulateOutcome())) {
            payment.setStatus(PaymentStatus.FAILED);          // simulated card/mobile failure
        } else {
            markPaid(payment);
        }
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    // The assigned driver confirms they received the cash -> PAID
    public PaymentResponse confirmCash(String paymentId, String callerId) {
        Payment payment = findPayment(paymentId);
        if (!callerId.equals(payment.getDriverAccountId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the driver of this ride can confirm the cash payment");
        }
        if (payment.getStatus() != PaymentStatus.AWAITING_DRIVER_CONFIRMATION) {
            throw new ApiException(HttpStatus.CONFLICT, "There is no cash payment waiting for confirmation");
        }
        markPaid(payment);
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    // View one payment (its passenger, its driver, or an admin)
    public PaymentResponse getPayment(String paymentId, String callerId, String callerRole) {
        Payment payment = findPayment(paymentId);
        requireInvolved(payment, callerId, callerRole);
        return PaymentResponse.from(payment);
    }

    // The payment of a ride (its passenger, its driver, or an admin)
    public PaymentResponse getPaymentForRide(String rideId, String callerId, String callerRole) {
        Payment payment = paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No payment for this ride"));
        requireInvolved(payment, callerId, callerRole);
        return PaymentResponse.from(payment);
    }

    // History: a passenger sees what they paid, a driver sees what they were paid
    public List<PaymentResponse> getMyPayments(String callerId, String callerRole) {
        List<Payment> payments = "DRIVER".equals(callerRole)
                ? paymentRepository.findByDriverAccountIdOrderByCreatedAtDesc(callerId)
                : paymentRepository.findByPassengerIdOrderByCreatedAtDesc(callerId);
        return payments.stream().map(PaymentResponse::from).toList();
    }

    // Receipt, only for PAID payments (its passenger, its driver, or an admin)
    public ReceiptResponse getReceipt(String paymentId, String callerId, String callerRole) {
        Payment payment = findPayment(paymentId);
        requireInvolved(payment, callerId, callerRole);
        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new ApiException(HttpStatus.CONFLICT, "A receipt is only available after the payment is PAID");
        }
        return ReceiptResponse.from(payment);
    }

    // ---------- helpers ----------

    private Payment findPayment(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    private void requireInvolved(Payment payment, String callerId, String callerRole) {
        boolean involved = callerId.equals(payment.getPassengerId())
                || callerId.equals(payment.getDriverAccountId())
                || "ADMIN".equals(callerRole);
        if (!involved) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not part of this payment");
        }
    }

    private void markPaid(Payment payment) {
        String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        payment.setStatus(PaymentStatus.PAID);
        payment.setTransactionReference("TXN-" + code);
        payment.setReceiptNumber("RCP-" + code);
        payment.setPaidAt(Instant.now());
    }
}

