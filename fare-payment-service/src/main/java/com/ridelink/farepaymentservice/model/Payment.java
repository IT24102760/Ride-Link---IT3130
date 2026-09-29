package com.ridelink.farepaymentservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

// One payment for one completed ride, stored in the "payments" collection
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;                    // paymentId: stored on the ride by the Ride service

    @Indexed(unique = true)
    private String rideId;                // one payment per ride

    private String passengerId;           // passenger's account id (their JWT sub)
    private String driverId;              // driver profile id (Driver service)
    private String driverAccountId;       // driver's account id (their JWT sub), for cash confirmation

    private VehicleType vehicleType;
    private String pickupPlace;
    private String destinationPlace;

    private FareBreakdown fareBreakdown;
    private BigDecimal finalFare;

    private PaymentStatus status;
    private PaymentMethod paymentMethod;
    private String transactionReference;  // set when PAID
    private String receiptNumber;         // set when PAID

    private Instant createdAt;
    private Instant paidAt;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRideId() { return rideId; }
    public void setRideId(String rideId) { this.rideId = rideId; }
    public String getPassengerId() { return passengerId; }
    public void setPassengerId(String passengerId) { this.passengerId = passengerId; }
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
    public String getDriverAccountId() { return driverAccountId; }
    public void setDriverAccountId(String driverAccountId) { this.driverAccountId = driverAccountId; }
    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
    public String getPickupPlace() { return pickupPlace; }
    public void setPickupPlace(String pickupPlace) { this.pickupPlace = pickupPlace; }
    public String getDestinationPlace() { return destinationPlace; }
    public void setDestinationPlace(String destinationPlace) { this.destinationPlace = destinationPlace; }
    public FareBreakdown getFareBreakdown() { return fareBreakdown; }
    public void setFareBreakdown(FareBreakdown fareBreakdown) { this.fareBreakdown = fareBreakdown; }
    public BigDecimal getFinalFare() { return finalFare; }
    public void setFinalFare(BigDecimal finalFare) { this.finalFare = finalFare; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
}


