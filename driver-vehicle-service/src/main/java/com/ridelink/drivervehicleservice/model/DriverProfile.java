package com.ridelink.drivervehicleservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

// A driver's work details, stored in the "driver_profiles" collection
@Document(collection = "driver_profiles")
public class DriverProfile {

    @Id
    private String id;                       // driverId: used by the Ride service to call us back

    @Indexed(unique = true)
    private String accountId;                // the driver's JWT "sub" (one profile per account)

    private String fullName;                 // copied from the token, for display
    private String licenceNumber;
    private String serviceArea;              // place name where the driver works, e.g. "Negombo"
    private String currentLocation;          // simulated current location (a place name)
    private Vehicle vehicle;

    private AvailabilityStatus availability;
    private Instant availableSince;          // when the driver last became AVAILABLE (used for fair matching)

    private Instant createdAt;
    private Instant updatedAt;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getLicenceNumber() { return licenceNumber; }
    public void setLicenceNumber(String licenceNumber) { this.licenceNumber = licenceNumber; }
    public String getServiceArea() { return serviceArea; }
    public void setServiceArea(String serviceArea) { this.serviceArea = serviceArea; }
    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public AvailabilityStatus getAvailability() { return availability; }
    public void setAvailability(AvailabilityStatus availability) { this.availability = availability; }
    public Instant getAvailableSince() { return availableSince; }
    public void setAvailableSince(Instant availableSince) { this.availableSince = availableSince; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
