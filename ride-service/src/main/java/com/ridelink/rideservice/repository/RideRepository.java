package com.ridelink.rideservice.repository;

import com.ridelink.rideservice.model.Ride;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

// Database access for rides (Spring writes the queries from the method names)
public interface RideRepository extends MongoRepository<Ride, String> {

    List<Ride> findByPassengerId(String passengerId);          // a passenger's rides

    List<Ride> findByDriverAccountId(String driverAccountId);  // a driver's rides
}

