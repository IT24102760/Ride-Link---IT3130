package com.ridelink.rideservice.service;

import com.ridelink.rideservice.client.DriverServiceClient;
import com.ridelink.rideservice.client.FareServiceClient;
import com.ridelink.rideservice.dto.AvailableDriver;
import com.ridelink.rideservice.dto.CreatePaymentRequest;
import com.ridelink.rideservice.dto.CreateRideRequest;
import com.ridelink.rideservice.dto.PaymentResult;
import com.ridelink.rideservice.dto.RideResponse;
import com.ridelink.rideservice.exception.ApiException;
import com.ridelink.rideservice.model.Ride;
import com.ridelink.rideservice.model.RideStatus;
import com.ridelink.rideservice.repository.RideRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

// Business rules for rides
@Service
public class RideService {

    private static final Logger log = LoggerFactory.getLogger(RideService.class);

    private final RideRepository rideRepository;
    private final DriverServiceClient driverClient;
    private final FareServiceClient fareClient;

    public RideService(RideRepository rideRepository, DriverServiceClient driverClient, FareServiceClient fareClient) {
        this.rideRepository = rideRepository;
        this.driverClient = driverClient;
        this.fareClient = fareClient;
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

    // Lists the drivers the passenger can choose from (asked from the Driver service, never stored here)
    public List<AvailableDriver> findDriversForRide(String rideId, String callerId, String callerRole, String bearerToken) {
        Ride ride = findRide(rideId);
        if (!isPassenger(ride, callerId) && !isAdmin(callerRole)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the passenger who requested this ride can see its drivers");
        }
        requireTransition(ride, RideStatus.ASSIGNED);   // only while the ride is REQUESTED
        return driverClient.findAvailableDrivers(ride.getPickup().placeName(), ride.getVehicleType(), bearerToken);
    }

    // Assign the longest-waiting available driver -> ASSIGNED
    public RideResponse assignDriver(String rideId, String callerId, String callerRole, String bearerToken) {
        return assignDriver(rideId, callerId, callerRole, bearerToken, null);
    }

    // Assign the driver the passenger chose, or the longest-waiting one if none was chosen -> ASSIGNED
    public RideResponse assignDriver(String rideId, String callerId, String callerRole, String bearerToken,
                                     String chosenDriverId) {
        Ride ride = findRide(rideId);
        if (!isPassenger(ride, callerId) && !isAdmin(callerRole)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the passenger who requested this ride can assign a driver");
        }
        requireTransition(ride, RideStatus.ASSIGNED);

        String area = ride.getPickup().placeName();
        List<AvailableDriver> drivers = driverClient.findAvailableDrivers(area, ride.getVehicleType(), bearerToken);
        if (drivers.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "No available " + ride.getVehicleType() + " driver in " + area);
        }

        // The passenger picked a driver: it must be in the list, and there is no fallback to others
        if (chosenDriverId != null && !chosenDriverId.isBlank()) {
            AvailableDriver chosen = drivers.stream()
                    .filter(d -> chosenDriverId.equals(d.driverId()))
                    .findFirst()
                    .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT,
                            "That driver is not available for this ride; choose another"));
            driverClient.setAvailability(chosen.driverId(), "BUSY");   // 409 here means someone else got them
            return saveAssignment(ride, chosen);
        }

        // Documented rule: take the first available driver; if another ride
        // grabbed them at the same moment (409), try the next one
        for (AvailableDriver driver : drivers) {
            try {
                driverClient.setAvailability(driver.driverId(), "BUSY");
            } catch (ApiException ex) {
                if (ex.getStatus() == HttpStatus.CONFLICT) {
                    continue;   // driver just became busy, try the next
                }
                throw ex;       // 502 / 503: stop, the ride stays REQUESTED
            }
            return saveAssignment(ride, driver);
        }
        throw new ApiException(HttpStatus.CONFLICT, "All available drivers were just taken, please try again");
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

    // Assigned driver finishes the trip -> Fare creates the bill -> COMPLETED
    public RideResponse completeRide(String rideId, String callerId) {
        Ride ride = findRide(rideId);
        requireAssignedDriver(ride, callerId);
        requireTransition(ride, RideStatus.COMPLETED);

        long tripMinutes = Math.max(1, Duration.between(ride.getStartedAt(), Instant.now()).toMinutes());
        PaymentResult payment = fareClient.createPayment(new CreatePaymentRequest(
                ride.getId(), ride.getPassengerId(), ride.getDriverId(), ride.getDriverAccountId(),
                ride.getVehicleType(), ride.getPickup(), ride.getDestination(), tripMinutes));

        releaseDriver(ride.getDriverId());   // driver can take new rides

        ride.setFinalFare(payment.finalFare());
        ride.setPaymentId(payment.paymentId());
        ride.setStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(Instant.now());
        return RideResponse.from(rideRepository.save(ride));
    }

    // Passenger, assigned driver or admin cancels -> CANCELLED
    public RideResponse cancelRide(String rideId, String callerId, String callerRole, String reason) {
        Ride ride = findRide(rideId);
        if (!isPassenger(ride, callerId) && !isAssignedDriver(ride, callerId) && !isAdmin(callerRole)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot cancel this ride");
        }
        changeStatus(ride, RideStatus.CANCELLED);
        if (ride.getDriverId() != null) {
            releaseDriver(ride.getDriverId());   // free the driver again
        }
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

    // Records the driver on the ride and moves it to ASSIGNED
    private RideResponse saveAssignment(Ride ride, AvailableDriver driver) {
        ride.setDriverId(driver.driverId());
        ride.setDriverAccountId(driver.accountId());
        ride.setStatus(RideStatus.ASSIGNED);
        return RideResponse.from(rideRepository.save(ride));
    }

    // Checks the lifecycle rules in RideStatus; invalid moves give 409
    private void requireTransition(Ride ride, RideStatus next) {
        if (!ride.getStatus().canTransitionTo(next)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Cannot change ride from " + ride.getStatus() + " to " + next);
        }
    }

    private void changeStatus(Ride ride, RideStatus next) {
        requireTransition(ride, next);
        ride.setStatus(next);
    }

    // Sets the driver AVAILABLE; a failure is logged, not thrown, so the ride itself is not blocked
    private void releaseDriver(String driverId) {
        try {
            driverClient.setAvailability(driverId, "AVAILABLE");
        } catch (ApiException ex) {
            log.warn("Could not set driver {} back to AVAILABLE: {}", driverId, ex.getMessage());
        }
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