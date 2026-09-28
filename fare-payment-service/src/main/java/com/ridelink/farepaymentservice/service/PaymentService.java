package com.ridelink.farepaymentservice.service;

import com.ridelink.farepaymentservice.dto.CreatePaymentRequest;
import com.ridelink.farepaymentservice.dto.PayRequest;
import com.ridelink.farepaymentservice.dto.PaymentResponse;
import com.ridelink.farepaymentservice.exception.ApiException;
import com.ridelink.farepaymentservice.model.FareBreakdown;
import com.ridelink.farepaymentservice.model.Payment;
import com.ridelink.farepaymentservice.model.PaymentStatus;
import com.ridelink.farepaymentservice.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final FareCalculator fareCalculator;

    public PaymentService(
            PaymentRepository paymentRepository,
            FareCalculator fareCalculator) {

        this.paymentRepository = paymentRepository;
        this.fareCalculator = fareCalculator;
    }

    public PaymentResponse createPayment(CreatePaymentRequest request) {

        // Prevent duplicate payment for the same ride
        if (paymentRepository.existsByRideId(request.rideId())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Payment already exists for this ride"
            );
        }

        // Calculate the final fare
        FareBreakdown fareBreakdown = fareCalculator.calculate(
                request.vehicleType(),
                request.pickup(),
                request.destination(),
                request.tripMinutes()
        );

        Payment payment = new Payment();

        payment.setRideId(request.rideId());
        payment.setPassengerId(request.passengerId());
        payment.setDriverId(request.driverId());
        payment.setVehicleType(request.vehicleType());

        // Pickup information
        payment.setPickupPlaceName(
                request.pickup().placeName()
        );

        payment.setPickupLatitude(
                request.pickup().latitude()
        );

        payment.setPickupLongitude(
                request.pickup().longitude()
        );

        // Destination information
        payment.setDestinationPlaceName(
                request.destination().placeName()
        );

        payment.setDestinationLatitude(
                request.destination().latitude()
        );

        payment.setDestinationLongitude(
                request.destination().longitude()
        );

        payment.setTripMinutes(
                request.tripMinutes()
        );

        // Store actual calculated distance
        payment.setDistanceKm(
                fareCalculator.calculateDistance(
                        request.pickup(),
                        request.destination()
                ).doubleValue()
        );

        payment.setFareBreakdown(fareBreakdown);
        payment.setFinalFare(fareBreakdown.getTotalFare());

        // New payments start as PENDING
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);

        return new PaymentResponse(
                savedPayment.getId(),
                savedPayment.getFinalFare(),
                savedPayment.getStatus()
        );
    }

    public PaymentResponse pay(
            String paymentId,
            String passengerId,
            PayRequest request) {

        // Find payment
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Payment not found"
                ));

        // Only the passenger who owns the payment can pay
        if (!payment.getPassengerId().equals(passengerId)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to pay for this payment"
            );
        }

        // A PAID payment cannot be paid again
        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Payment has already been completed"
            );
        }

        // Only PENDING and FAILED payments can be processed
        if (payment.getStatus() != PaymentStatus.PENDING
                && payment.getStatus() != PaymentStatus.FAILED) {

            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Payment cannot be processed"
            );
        }

        payment.setPaymentMethod(
                request.paymentMethod()
        );

        // Simulate a failed payment
        if ("FAIL".equalsIgnoreCase(request.simulateOutcome())) {

            payment.setStatus(PaymentStatus.FAILED);

            Payment savedPayment =
                    paymentRepository.save(payment);

            return new PaymentResponse(
                    savedPayment.getId(),
                    savedPayment.getFinalFare(),
                    savedPayment.getStatus()
            );
        }

        // Successful payment
        payment.setStatus(PaymentStatus.PAID);

        payment.setTransactionReference(
                generateTransactionReference()
        );

        payment.setReceiptNumber(
                generateReceiptNumber()
        );

        payment.setPaidAt(
                LocalDateTime.now()
        );

        Payment savedPayment =
                paymentRepository.save(payment);

        return new PaymentResponse(
                savedPayment.getId(),
                savedPayment.getFinalFare(),
                savedPayment.getStatus()
        );
    }

    private String generateTransactionReference() {

        return "TXN-" + System.currentTimeMillis();
    }

    private String generateReceiptNumber() {

        return "REC-" + System.currentTimeMillis();
    }
}

