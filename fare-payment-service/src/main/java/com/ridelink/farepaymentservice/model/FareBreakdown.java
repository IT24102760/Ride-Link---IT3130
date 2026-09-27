package com.ridelink.farepaymentservice.model;

import java.math.BigDecimal;
public class FareBreakdown {
    private BigDecimal baseFare;
    private BigDecimal distanceCharge;
    private BigDecimal timeCharge;
    private BigDecimal minimumFare;
    private boolean minimumApplied;
    private BigDecimal totalFare;

    public FareBreakdown() {
    }

    public FareBreakdown(
            BigDecimal baseFare,
            BigDecimal distanceCharge,
            BigDecimal timeCharge,
            BigDecimal minimumFare,
            boolean minimumApplied,
            BigDecimal totalFare) {

        this.baseFare = baseFare;
        this.distanceCharge = distanceCharge;
        this.timeCharge = timeCharge;
        this.minimumFare = minimumFare;
        this.minimumApplied = minimumApplied;
        this.totalFare = totalFare;
    }

    public BigDecimal getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(BigDecimal baseFare) {
        this.baseFare = baseFare;
    }

    public BigDecimal getDistanceCharge() {
        return distanceCharge;
    }

    public void setDistanceCharge(BigDecimal distanceCharge) {
        this.distanceCharge = distanceCharge;
    }

    public BigDecimal getTimeCharge() {
        return timeCharge;
    }

    public void setTimeCharge(BigDecimal timeCharge) {
        this.timeCharge = timeCharge;
    }

    public BigDecimal getMinimumFare() {
        return minimumFare;
    }

    public void setMinimumFare(BigDecimal minimumFare) {
        this.minimumFare = minimumFare;
    }

    public boolean isMinimumApplied() {
        return minimumApplied;
    }

    public void setMinimumApplied(boolean minimumApplied) {
        this.minimumApplied = minimumApplied;
    }

    public BigDecimal getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(BigDecimal totalFare) {
        this.totalFare = totalFare;
    }
}

