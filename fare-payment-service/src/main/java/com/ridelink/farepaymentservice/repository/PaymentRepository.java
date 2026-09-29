package com.ridelink.farepaymentservice.repository;

import com.ridelink.farepaymentservice.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

// Database access for payments (Spring writes the queries from the method names)
public interface PaymentRepository extends MongoRepository<Payment, String> {

    Optional<Payment> findByRideId(String rideId);

    List<Payment> findByPassengerIdOrderByCreatedAtDesc(String passengerId);        // passenger history

    List<Payment> findByDriverAccountIdOrderByCreatedAtDesc(String driverAccountId); // driver history
}
