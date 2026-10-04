package com.ridelink.rideservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

// One ride, stored in the "rides" collection
@Document(collection = "rides")
public class Ride {

    @Id
    private String id;

    private String passengerId;      // passenger's account id (from JWT)
    private String driverId;         // driver profile id (from Driver service)
    private String driverAccountId;  // driver's account id (for permission checks)

    private VehicleType vehicleType;
    private Location pickup;
    private Location destination;
    private RideStatus status;

    private BigDecimal finalFare;    // set when the ride is completed
    private String paymentId;        // payment created by Fare service
    private String cancelReason;

    private Instant requestedAt;
    private Instant startedAt;       // used to calculate trip minutes
    private Instant completedAt;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPassengerId() { return passengerId; }
    public void setPassengerId(String passengerId) { this.passengerId = passengerId; }
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
    public String getDriverAccountId() { return driverAccountId; }
    public void setDriverAccountId(String driverAccountId) { this.driverAccountId = driverAccountId; }
    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
    public Location getPickup() { return pickup; }
    public void setPickup(Location pickup) { this.pickup = pickup; }
    public Location getDestination() { return destination; }
    public void setDestination(Location destination) { this.destination = destination; }
    public RideStatus getStatus() { return status; }
    public void setStatus(RideStatus status) { this.status = status; }
    public BigDecimal getFinalFare() { return finalFare; }
    public void setFinalFare(BigDecimal finalFare) { this.finalFare = finalFare; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public Instant getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Instant requestedAt) { this.requestedAt = requestedAt; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}