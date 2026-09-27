package com.ridelink.farepaymentservice.repository;

import com.ridelink.farepaymentservice.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {

    Optional<Payment> findByRideId(String rideId);

    boolean existsByRideId(String rideId);

    List<Payment> findByPassengerIdOrderByCreatedAtDesc(String passengerId);
}
