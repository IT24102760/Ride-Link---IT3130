package com.ridelink.farepaymentservice.service;

import com.ridelink.farepaymentservice.dto.ReceiptResponse;
import com.ridelink.farepaymentservice.exception.ApiException;
import com.ridelink.farepaymentservice.model.Payment;
import com.ridelink.farepaymentservice.model.PaymentStatus;
import com.ridelink.farepaymentservice.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReceiptService {

    private final PaymentRepository paymentRepository;

    public ReceiptService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public ReceiptResponse getReceipt(
            String paymentId,
            String passengerId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Payment not found"
                ));

        if (!payment.getPassengerId().equals(passengerId)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to access this receipt"
            );
        }

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Receipt is available only for paid payments"
            );
        }

        return new ReceiptResponse(
                payment.getId(),
                payment.getRideId(),
                payment.getPassengerId(),
                payment.getDriverId(),
                payment.getFinalFare(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getTransactionReference(),
                payment.getReceiptNumber(),
                payment.getPaidAt()
        );
    }
}
