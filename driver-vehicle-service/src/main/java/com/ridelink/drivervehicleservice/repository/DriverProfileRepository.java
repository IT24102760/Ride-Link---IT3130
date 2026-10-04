package com.ridelink.drivervehicleservice.repository;

import com.ridelink.drivervehicleservice.model.AvailabilityStatus;
import com.ridelink.drivervehicleservice.model.DriverProfile;
import com.ridelink.drivervehicleservice.model.VehicleType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

// Database access for driver profiles (Spring writes the queries from the method names)
public interface DriverProfileRepository extends MongoRepository<DriverProfile, String> {

    Optional<DriverProfile> findByAccountId(String accountId);   // "my" profile, from the token

    boolean existsByAccountId(String accountId);

    boolean existsByVehiclePlateNumber(String plateNumber);      // one vehicle per plate

    // Matching rule: AVAILABLE drivers in the area with the right vehicle, longest-waiting first
    List<DriverProfile> findByAvailabilityAndServiceAreaIgnoreCaseAndVehicleTypeOrderByAvailableSinceAsc(
            AvailabilityStatus availability, String serviceArea, VehicleType vehicleType);
}
