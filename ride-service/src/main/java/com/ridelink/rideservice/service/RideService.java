package com.ridelink.rideservice.service;

import com.ridelink.rideservice.dto.CreateRideRequest;
import com.ridelink.rideservice.dto.RideResponse;
import com.ridelink.rideservice.exception.ApiException;
import com.ridelink.rideservice.model.Ride;
import com.ridelink.rideservice.model.RideStatus;
import com.ridelink.rideservice.repository.RideRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

// Business rules for rides
@Service
public class RideService {

    private final RideRepository rideRepository;

    public RideService(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    // Passenger requests a ride -> REQUESTED
    public RideResponse requestRide(String passengerId, CreateRideRequest request) {
        Ride ride = new Ride();
        ride.setPassengerId(passengerId);
        ride.setPickup(request.pickup());
        ride.setDestination(request.destination());
        ride.setVehicleType(request.vehicleType());
        ride.setStatus(RideStatus.REQUESTED);
        ride.setRequestedAt(Instant.now());
        return RideResponse.from(rideRepository.save(ride));
    }

    // Assigned driver accepts -> ACCEPTED
    public RideResponse acceptRide(String rideId, String callerId) {
        Ride ride = findRide(rideId);
        requireAssignedDriver(ride, callerId);
        changeStatus(ride, RideStatus.ACCEPTED);
        return RideResponse.from(rideRepository.save(ride));
    }

    // Assigned driver picks up the passenger -> IN_PROGRESS
    public RideResponse startRide(String rideId, String callerId) {
        Ride ride = findRide(rideId);
        requireAssignedDriver(ride, callerId);
        changeStatus(ride, RideStatus.IN_PROGRESS);
        ride.setStartedAt(Instant.now());
        return RideResponse.from(rideRepository.save(ride));
    }

    // Passenger, assigned driver or admin cancels -> CANCELLED
    public RideResponse cancelRide(String rideId, String callerId, String callerRole, String reason) {
        Ride ride = findRide(rideId);
        if (!isPassenger(ride, callerId) && !isAssignedDriver(ride, callerId) && !isAdmin(callerRole)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot cancel this ride");
        }
        changeStatus(ride, RideStatus.CANCELLED);
        ride.setCancelReason(reason);
        return RideResponse.from(rideRepository.save(ride));
    }

    // View one ride (only people involved, or admin)
    public RideResponse getRide(String rideId, String callerId, String callerRole) {
        Ride ride = findRide(rideId);
        if (!isPassenger(ride, callerId) && !isAssignedDriver(ride, callerId) && !isAdmin(callerRole)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot view this ride");
        }
        return RideResponse.from(ride);
    }

    // List the caller's own rides
    public List<RideResponse> getMyRides(String callerId, String callerRole) {
        List<Ride> rides = "DRIVER".equals(callerRole)
                ? rideRepository.findByDriverAccountId(callerId)
                : rideRepository.findByPassengerId(callerId);
        return rides.stream().map(RideResponse::from).toList();
    }

    // ---------- helpers ----------

    private Ride findRide(String rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Ride not found"));
    }

    // Uses the lifecycle rules in RideStatus; invalid moves give 409
    private void changeStatus(Ride ride, RideStatus next) {
        if (!ride.getStatus().canTransitionTo(next)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Cannot change ride from " + ride.getStatus() + " to " + next);
        }
        ride.setStatus(next);
    }

    private void requireAssignedDriver(Ride ride, String callerId) {
        if (!isAssignedDriver(ride, callerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the assigned driver can do this");
        }
    }

    private boolean isPassenger(Ride ride, String callerId) {
        return ride.getPassengerId().equals(callerId);
    }

    private boolean isAssignedDriver(Ride ride, String callerId) {
        return callerId.equals(ride.getDriverAccountId());
    }

    private boolean isAdmin(String role) {
        return "ADMIN".equals(role);
    }
}